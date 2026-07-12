package com.example.chemlearn.payment.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.lms.repository.UserRepository;
import com.example.chemlearn.payment.dto.CheckoutResponseData;
import com.example.chemlearn.payment.dto.LearningPackageResponse;
import com.example.chemlearn.payment.dto.PaymentData;
import com.example.chemlearn.payment.dto.UserPackageEntitlementResponse;
import com.example.chemlearn.payment.entity.LearningPackage;
import com.example.chemlearn.payment.entity.PaymentTransaction;
import com.example.chemlearn.payment.entity.UserPackageEntitlement;
import com.example.chemlearn.payment.enums.EntitlementStatus;
import com.example.chemlearn.payment.enums.PaymentStatus;
import com.example.chemlearn.payment.repository.LearningPackageRepository;
import com.example.chemlearn.payment.repository.PaymentTransactionRepository;
import com.example.chemlearn.payment.repository.UserPackageEntitlementRepository;
import com.example.chemlearn.payment.service.PaymentService;
import com.example.chemlearn.util.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.webhooks.WebhookData;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private static final long CHECKOUT_EXPIRY_SECONDS = 15 * 60;
    private static final String PAYMENT_RETURN_PATH = "/student/subscriptions";

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final LearningPackageRepository learningPackageRepository;
    private final UserPackageEntitlementRepository userPackageEntitlementRepository;
    private final UserRepository userRepository;
    private final PayOS payOS;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    @Value("${app.frontend.allowed-origins:http://localhost:5173}")
    private String allowedFrontendOrigins;

    @Override
    @Transactional
    public CheckoutResponseData createPaymentLink(PaymentData request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        LearningPackage learningPackage = learningPackageRepository.findByPackageCode(request.getPackageCode())
            .filter(LearningPackage::getIsActive)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Package not found or inactive"));

        lockUserPackagePayment(currentUserId, learningPackage.getPackageCode());
        ensureNoActiveEntitlement(currentUserId, learningPackage.getPackageCode());

        PaymentTransaction pendingTransaction = paymentTransactionRepository
                .findTopByUserIdAndPackageCodeAndStatusOrderByCreatedAtDesc(
                        currentUserId,
                        learningPackage.getPackageCode(),
                        PaymentStatus.PENDING)
                .orElse(null);

        if (pendingTransaction != null) {
            reconcileFromPayOS(pendingTransaction, false);
            if (pendingTransaction.getStatus() == PaymentStatus.PAID) {
                return toResponse(pendingTransaction);
            }

            if (pendingTransaction.getStatus() == PaymentStatus.PENDING
                    && !isBlank(pendingTransaction.getCheckoutUrl())) {
                return CheckoutResponseData.builder()
                        .orderCode(pendingTransaction.getOrderCode())
                        .userId(pendingTransaction.getUserId())
                        .packageCode(pendingTransaction.getPackageCode())
                        .amount(pendingTransaction.getAmount())
                        .status(pendingTransaction.getStatus().name())
                        .paymentLinkId(pendingTransaction.getPaymentLinkId())
                        .checkoutUrl(pendingTransaction.getCheckoutUrl())
                        .qrCode(pendingTransaction.getQrCode())
                        .message("Existing pending checkout returned")
                        .build();
            }
        }

        Long amount = resolveAmount(learningPackage);
        Long orderCode = generateOrderCode();
        String buyerName = user.getFullName();
        String buyerEmail = user.getEmail();

        String returnUrl = buildPaymentRedirectUrl(request.getReturnUrl(), orderCode);
        String cancelUrl = buildPaymentRedirectUrl(request.getCancelUrl(), orderCode);

        CreatePaymentLinkRequest payosRequest = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(amount)
                .description(buildPaymentDescription(learningPackage))
                .cancelUrl(cancelUrl)
                .returnUrl(returnUrl)
                .buyerName(buyerName)
                .buyerEmail(buyerEmail)
                .expiredAt(Instant.now().plusSeconds(CHECKOUT_EXPIRY_SECONDS).getEpochSecond())
                .build();

        CreatePaymentLinkResponse payosResponse = payOS.paymentRequests().create(payosRequest);
        validateCreatePaymentResponse(payosResponse, orderCode, amount);

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrderCode(payosResponse.getOrderCode());
        transaction.setUserId(user.getId());
        transaction.setPackageCode(learningPackage.getPackageCode());
        transaction.setAmount(payosResponse.getAmount());
        transaction.setStatus(mapStatus(payosResponse.getStatus()));
        transaction.setPaymentLinkId(payosResponse.getPaymentLinkId());
        transaction.setCheckoutUrl(payosResponse.getCheckoutUrl());
        transaction.setQrCode(payosResponse.getQrCode());
        transaction.setBuyerName(buyerName);
        transaction.setBuyerEmail(buyerEmail);
        transaction.setPayosRequest(writeJson(toPayOSRequestAudit(payosRequest)));
        transaction.setPayosResponse(writeJson(toPayOSCreateResponseAudit(payosResponse)));
        paymentTransactionRepository.save(transaction);

        return toResponse(transaction);
    }

    @Override
    @Transactional
    public CheckoutResponseData getPaymentStatus(Long orderCode) {
        PaymentTransaction transaction = paymentTransactionRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment transaction not found"));
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        if (!transaction.getUserId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Payment transaction does not belong to current user");
        }
        reconcileFromPayOS(transaction, false);
        return toResponse(transaction);
    }

    @Override
    @Transactional
    public CheckoutResponseData handleWebhook(Map<String, Object> webhookBody) {
        WebhookData webhookData = payOS.webhooks().verify(webhookBody);
        PaymentTransaction transaction = paymentTransactionRepository.findByOrderCode(webhookData.getOrderCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment transaction not found"));

        if (!transaction.getAmount().equals(webhookData.getAmount())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment amount does not match");
        }

        if (!isBlank(transaction.getPaymentLinkId())
                && !transaction.getPaymentLinkId().equals(webhookData.getPaymentLinkId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment link id does not match");
        }

        transaction.setWebhookPayload(writeJson(webhookBody));
        paymentTransactionRepository.save(transaction);
        reconcileFromPayOS(transaction, true);

        return CheckoutResponseData.builder()
                .orderCode(transaction.getOrderCode())
                .userId(transaction.getUserId())
                .packageCode(transaction.getPackageCode())
                .amount(transaction.getAmount())
                .status(transaction.getStatus().name())
                .paymentLinkId(transaction.getPaymentLinkId())
                .checkoutUrl(transaction.getCheckoutUrl())
                .qrCode(transaction.getQrCode())
                .message("Webhook processed successfully")
                .build();
    }

    @Override
    public List<LearningPackageResponse> getActivePackages() {
        return learningPackageRepository.findAllByIsActiveTrueOrderByGradeLevelAsc().stream()
                .map(this::toPackageResponse)
                .toList();
    }

    @Override
    public LearningPackageResponse getPackage(String packageCode) {
        LearningPackage learningPackage = learningPackageRepository.findByPackageCode(packageCode)
                .filter(LearningPackage::getIsActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Package not found or inactive"));
        return toPackageResponse(learningPackage);
    }

    @Override
    public List<UserPackageEntitlementResponse> getCurrentUserEntitlements() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return userPackageEntitlementRepository.findByUserIdAndStatusIn(currentUserId, accessGrantingStatuses()).stream()
                .map(this::expireIfNeeded)
                .filter(this::grantsAccess)
                .map(this::toEntitlementResponse)
                .toList();
    }

    @Override
    public UserPackageEntitlementResponse getCurrentUserEntitlement(String packageCode) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        UserPackageEntitlement entitlement = userPackageEntitlementRepository
                .findByUserIdAndPackageCodeAndStatusIn(currentUserId, packageCode, accessGrantingStatuses())
                .map(this::expireIfNeeded)
                .filter(this::grantsAccess)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active entitlement not found"));
        return toEntitlementResponse(entitlement);
    }



    private CheckoutResponseData toResponse(PaymentTransaction transaction) {
        return CheckoutResponseData.builder()
                .orderCode(transaction.getOrderCode())
                .userId(transaction.getUserId())
                .packageCode(transaction.getPackageCode())
                .amount(transaction.getAmount())
                .status(transaction.getStatus().name())
                .paymentLinkId(transaction.getPaymentLinkId())
                .checkoutUrl(transaction.getCheckoutUrl())
                .qrCode(transaction.getQrCode())
                .message("Payment transaction retrieved successfully")
                .build();
    }

    private LearningPackageResponse toPackageResponse(LearningPackage learningPackage) {
        return LearningPackageResponse.builder()
                .packageCode(learningPackage.getPackageCode())
                .gradeLevel(learningPackage.getGradeLevel())
                .packageName(learningPackage.getPackageName())
                .description(learningPackage.getDescription())
                .basePrice(learningPackage.getBasePrice())
                .durationDays(learningPackage.getDurationDays())
                .benefitsJson(learningPackage.getBenefitsJson())
                .isActive(learningPackage.getIsActive())
                .build();
    }

    private UserPackageEntitlementResponse toEntitlementResponse(UserPackageEntitlement entitlement) {
        return UserPackageEntitlementResponse.builder()
                .id(entitlement.getId())
                .userId(entitlement.getUserId())
                .packageCode(entitlement.getPackageCode())
                .paymentTransactionId(entitlement.getPaymentTransactionId())
                .status(entitlement.getStatus().name())
                .startAt(entitlement.getStartAt())
                .endAt(entitlement.getEndAt())
                .cancelledAt(entitlement.getCancelledAt())
                .cancellationReason(entitlement.getCancellationReason())
                .metadataJson(entitlement.getMetadataJson())
                .build();
    }

    private void ensureEntitlement(PaymentTransaction transaction) {
        userPackageEntitlementRepository.findByPaymentTransactionId(transaction.getId()).orElseGet(() -> {
            LearningPackage learningPackage = learningPackageRepository.findByPackageCode(transaction.getPackageCode())
                    .filter(LearningPackage::getIsActive)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Package not found or inactive"));

            UserPackageEntitlement entitlement = UserPackageEntitlement.builder()
                    .userId(transaction.getUserId())
                    .packageCode(transaction.getPackageCode())
                    .paymentTransactionId(transaction.getId())
                    .status(EntitlementStatus.ACTIVE)
                    .startAt(transaction.getPaidAt() != null ? transaction.getPaidAt() : java.time.LocalDateTime.now())
                    .endAt(resolveEndAt(learningPackage, transaction.getPaidAt()))
                    .metadataJson(buildEntitlementMetadata(learningPackage, transaction))
                    .build();
            return userPackageEntitlementRepository.save(entitlement);
        });
    }

    private void ensureNoActiveEntitlement(UUID userId, String packageCode) {
        userPackageEntitlementRepository.findByUserIdAndPackageCodeAndStatusIn(
                userId,
                packageCode,
                accessGrantingStatuses()
        )
                .map(this::expireIfNeeded)
                .filter(this::grantsAccess)
                .ifPresent(entitlement -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "User already has an active entitlement for this package");
                });
    }

    private void reconcileFromPayOS(PaymentTransaction transaction, boolean failOnReconcileError) {
        if (transaction.getStatus() == PaymentStatus.PAID) {
            ensureEntitlement(transaction);
            return;
        }

        try {
            PaymentLink paymentLink = payOS.paymentRequests().get(transaction.getOrderCode());
            if (!transaction.getOrderCode().equals(paymentLink.getOrderCode())) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "PayOS order code mismatch");
            }

            if (!transaction.getAmount().equals(paymentLink.getAmount())) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "PayOS amount mismatch");
            }

            PaymentStatus latestStatus = mapStatus(paymentLink.getStatus());
            log.info(
                    "Reconciled PayOS payment orderCode={}, localStatus={}, payosStatus={}, amountPaid={}, amountRemaining={}",
                    transaction.getOrderCode(),
                    transaction.getStatus(),
                    paymentLink.getStatus(),
                    paymentLink.getAmountPaid(),
                    paymentLink.getAmountRemaining()
            );
            transaction.setStatus(latestStatus);
            transaction.setPaymentLinkId(paymentLink.getId());
            transaction.setPayosResponse(writeJson(toPayOSPaymentLinkAudit(paymentLink)));

            if (latestStatus == PaymentStatus.PAID) {
                if (transaction.getPaidAt() == null) {
                    transaction.setPaidAt(resolvePaidAt(paymentLink));
                }
                paymentTransactionRepository.save(transaction);
                ensureEntitlement(transaction);
                return;
            }

            paymentTransactionRepository.save(transaction);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.warn(
                    "Unable to reconcile PayOS payment status for orderCode={}, localStatus={}, paymentLinkId={}",
                    transaction.getOrderCode(),
                    transaction.getStatus(),
                    transaction.getPaymentLinkId(),
                    e
            );
            if (failOnReconcileError) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to verify PayOS payment status", e);
            }
        }
    }

    private java.time.LocalDateTime resolvePaidAt(PaymentLink paymentLink) {
        if (paymentLink.getTransactions() != null && !paymentLink.getTransactions().isEmpty()) {
            return paymentLink.getTransactions().stream()
                    .map(vn.payos.model.v2.paymentRequests.Transaction::getTransactionDateTime)
                    .filter(java.util.Objects::nonNull)
                    .findFirst()
                    .map(transactionDateTime -> transactionDateTime.toLocalDateTime())
                    .orElse(java.time.LocalDateTime.now());
        }
        return java.time.LocalDateTime.now();
    }

    private void lockUserPackagePayment(UUID userId, String packageCode) {
        entityManager
                .createNativeQuery("SELECT pg_advisory_xact_lock(hashtext(?1), hashtext(?2))")
                .setParameter(1, userId.toString())
                .setParameter(2, packageCode)
                .getSingleResult();
    }

    private java.time.LocalDateTime resolveEndAt(LearningPackage learningPackage, java.time.LocalDateTime paidAt) {
        if (learningPackage.getDurationDays() == null) {
            return null;
        }

        java.time.LocalDateTime startAt = paidAt != null ? paidAt : java.time.LocalDateTime.now();
        return startAt.plusDays(learningPackage.getDurationDays());
    }

    private String buildEntitlementMetadata(LearningPackage learningPackage, PaymentTransaction transaction) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("packageCode", learningPackage.getPackageCode());
            metadata.put("gradeLevel", learningPackage.getGradeLevel());
            metadata.put("paymentTransactionId", transaction.getId().toString());
            metadata.put("paymentLinkId", transaction.getPaymentLinkId());
            metadata.put("status", "ACTIVE");
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize entitlement metadata", e);
        }
    }

    private String buildCancellationMetadata(UserPackageEntitlement entitlement) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("packageCode", entitlement.getPackageCode());
            metadata.put("paymentTransactionId", entitlement.getPaymentTransactionId().toString());
            metadata.put("paymentLinkId", findPaymentLinkId(entitlement));
            metadata.put("status", entitlement.getStatus().name());
            metadata.put("cancelledAt", entitlement.getCancelledAt() != null ? entitlement.getCancelledAt().toString() : null);
            metadata.put("accessUntil", entitlement.getEndAt() != null ? entitlement.getEndAt().toString() : null);
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize entitlement metadata", e);
        }
    }

    private String findPaymentLinkId(UserPackageEntitlement entitlement) {
        return paymentTransactionRepository.findById(entitlement.getPaymentTransactionId())
                .map(PaymentTransaction::getPaymentLinkId)
                .orElse(null);
    }

    private List<EntitlementStatus> accessGrantingStatuses() {
        return List.of(EntitlementStatus.ACTIVE, EntitlementStatus.CANCELLED);
    }

    private UserPackageEntitlement expireIfNeeded(UserPackageEntitlement entitlement) {
        if (entitlement.getEndAt() == null || entitlement.getEndAt().isAfter(java.time.LocalDateTime.now())) {
            return entitlement;
        }

        if (entitlement.getStatus() != EntitlementStatus.EXPIRED) {
            entitlement.setStatus(EntitlementStatus.EXPIRED);
            return userPackageEntitlementRepository.save(entitlement);
        }

        return entitlement;
    }

    private boolean grantsAccess(UserPackageEntitlement entitlement) {
        if (!accessGrantingStatuses().contains(entitlement.getStatus())) {
            return false;
        }

        return entitlement.getEndAt() == null || entitlement.getEndAt().isAfter(java.time.LocalDateTime.now());
    }

    private Long resolveAmount(LearningPackage learningPackage) {
        if (learningPackage.getBasePrice() != null && learningPackage.getBasePrice() > 0) {
            return learningPackage.getBasePrice();
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Package price is not configured");
    }

    private String buildPaymentDescription(LearningPackage learningPackage) {
        return "Mua " + learningPackage.getPackageCode();
    }

    private Long generateOrderCode() {
        Long orderCode;
        do {
            orderCode = (System.currentTimeMillis() * 100) + ThreadLocalRandom.current().nextLong(10, 100);
        } while (paymentTransactionRepository.existsByOrderCode(orderCode));
        return orderCode;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String buildPaymentRedirectUrl(String url, Long orderCode) {
        if (isBlank(url) || orderCode == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment redirect URL is required");
        }

        URI uri = parseRedirectUrl(url);
        validatePaymentRedirectUrl(uri);

        String nextQuery = uri.getQuery();
        nextQuery = isBlank(nextQuery) ? "orderCode=" + orderCode : nextQuery + "&orderCode=" + orderCode;

        try {
            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), nextQuery, null).toString();
        } catch (URISyntaxException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment redirect URL", e);
        }
    }

    private URI parseRedirectUrl(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment redirect URL", e);
        }
    }

    private void validatePaymentRedirectUrl(URI uri) {
        if (uri.getScheme() == null || uri.getHost() == null || uri.getUserInfo() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment redirect URL");
        }

        String scheme = uri.getScheme().toLowerCase();
        if (!scheme.equals("http") && !scheme.equals("https")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment redirect URL scheme");
        }

        if (!PAYMENT_RETURN_PATH.equals(uri.getPath())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment redirect URL path");
        }

        String origin = normalizeOrigin(uri);
        boolean allowed = allowedOrigins().stream().anyMatch(origin::equals);
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment redirect origin is not allowed");
        }
    }

    private void validateCreatePaymentResponse(CreatePaymentLinkResponse response, Long orderCode, Long amount) {
        if (!orderCode.equals(response.getOrderCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "PayOS order code mismatch");
        }

        if (!amount.equals(response.getAmount())) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "PayOS amount mismatch");
        }
    }

    private List<String> allowedOrigins() {
        return java.util.Arrays.stream(allowedFrontendOrigins.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(this::parseRedirectUrl)
                .map(this::normalizeOrigin)
                .distinct()
                .toList();
    }

    private String normalizeOrigin(URI uri) {
        String scheme = uri.getScheme().toLowerCase();
        String host = uri.getHost().toLowerCase();
        int port = uri.getPort();
        boolean defaultPort = (scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443);
        return port < 0 || defaultPort ? scheme + "://" + host : scheme + "://" + host + ":" + port;
    }

    private PaymentStatus mapStatus(vn.payos.model.v2.paymentRequests.PaymentLinkStatus payosStatus) {
        if (payosStatus == null) {
            return PaymentStatus.PENDING;
        }

        return switch (payosStatus) {
            case PAID -> PaymentStatus.PAID;
            case CANCELLED -> PaymentStatus.CANCELLED;
            case EXPIRED -> PaymentStatus.EXPIRED;
            case FAILED -> PaymentStatus.FAILED;
            default -> PaymentStatus.PENDING;
        };
    }

    private Map<String, Object> toPayOSRequestAudit(CreatePaymentLinkRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderCode", request.getOrderCode());
        payload.put("amount", request.getAmount());
        payload.put("description", request.getDescription());
        payload.put("cancelUrl", request.getCancelUrl());
        payload.put("returnUrl", request.getReturnUrl());
        payload.put("buyerName", request.getBuyerName());
        payload.put("buyerEmail", request.getBuyerEmail());
        payload.put("buyerPhone", request.getBuyerPhone());
        payload.put("expiredAt", request.getExpiredAt());
        return payload;
    }

    private Map<String, Object> toPayOSCreateResponseAudit(CreatePaymentLinkResponse response) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("bin", response.getBin());
        payload.put("accountNumber", response.getAccountNumber());
        payload.put("accountName", response.getAccountName());
        payload.put("amount", response.getAmount());
        payload.put("description", response.getDescription());
        payload.put("orderCode", response.getOrderCode());
        payload.put("currency", response.getCurrency());
        payload.put("paymentLinkId", response.getPaymentLinkId());
        payload.put("status", response.getStatus() != null ? response.getStatus().name() : null);
        payload.put("expiredAt", response.getExpiredAt());
        payload.put("checkoutUrl", response.getCheckoutUrl());
        payload.put("qrCode", response.getQrCode());
        return payload;
    }

    private Map<String, Object> toPayOSPaymentLinkAudit(PaymentLink paymentLink) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", paymentLink.getId());
        payload.put("orderCode", paymentLink.getOrderCode());
        payload.put("amount", paymentLink.getAmount());
        payload.put("amountPaid", paymentLink.getAmountPaid());
        payload.put("amountRemaining", paymentLink.getAmountRemaining());
        payload.put("status", paymentLink.getStatus() != null ? paymentLink.getStatus().name() : null);
        payload.put("createdAt", paymentLink.getCreatedAt() != null ? paymentLink.getCreatedAt().toString() : null);
        payload.put("cancellationReason", paymentLink.getCancellationReason());
        payload.put("canceledAt", paymentLink.getCanceledAt() != null ? paymentLink.getCanceledAt().toString() : null);
        payload.put("transactions", paymentLink.getTransactions() == null
                ? List.of()
                : paymentLink.getTransactions().stream()
                        .map(this::toPayOSTransactionAudit)
                        .toList());
        return payload;
    }

    private Map<String, Object> toPayOSTransactionAudit(vn.payos.model.v2.paymentRequests.Transaction transaction) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reference", transaction.getReference());
        payload.put("amount", transaction.getAmount());
        payload.put("accountNumber", transaction.getAccountNumber());
        payload.put("description", transaction.getDescription());
        payload.put("transactionDateTime", transaction.getTransactionDateTime() != null
                ? transaction.getTransactionDateTime().toString()
                : null);
        payload.put("virtualAccountName", transaction.getVirtualAccountName());
        payload.put("virtualAccountNumber", transaction.getVirtualAccountNumber());
        payload.put("counterAccountBankId", transaction.getCounterAccountBankId());
        payload.put("counterAccountBankName", transaction.getCounterAccountBankName());
        payload.put("counterAccountName", transaction.getCounterAccountName());
        payload.put("counterAccountNumber", transaction.getCounterAccountNumber());
        return payload;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize payment payload", e);
        }
    }
}
