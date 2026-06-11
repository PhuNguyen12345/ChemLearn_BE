package com.example.chemlearn.payment.service.impl;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.lms.repository.UserRepository;
import com.example.chemlearn.payment.dto.CheckoutResponseData;
import com.example.chemlearn.payment.dto.PaymentData;
import com.example.chemlearn.payment.entity.LearningPackage;
import com.example.chemlearn.payment.entity.PaymentTransaction;
import com.example.chemlearn.payment.entity.UserPackageEntitlement;
import com.example.chemlearn.payment.enums.EntitlementStatus;
import com.example.chemlearn.payment.enums.PaymentStatus;
import com.example.chemlearn.payment.repository.LearningPackageRepository;
import com.example.chemlearn.payment.repository.PaymentTransactionRepository;
import com.example.chemlearn.payment.repository.UserPackageEntitlementRepository;
import com.example.chemlearn.payment.service.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.WebhookData;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final LearningPackageRepository learningPackageRepository;
    private final UserPackageEntitlementRepository userPackageEntitlementRepository;
    private final UserRepository userRepository;
    private final PayOS payOS;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public CheckoutResponseData createPaymentLink(PaymentData request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        LearningPackage learningPackage = learningPackageRepository.findByPackageCode(request.getPackageCode())
            .filter(LearningPackage::getIsActive)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Package not found or inactive"));

        PaymentTransaction existingTransaction = paymentTransactionRepository.findByOrderCode(request.getOrderCode())
                .orElse(null);

        if (existingTransaction != null && existingTransaction.getCheckoutUrl() != null) {
            return toResponse(existingTransaction);
        }

        CreatePaymentLinkRequest payosRequest = CreatePaymentLinkRequest.builder()
                .orderCode(request.getOrderCode())
                .amount(request.getAmount())
                .description(request.getDescription())
                .cancelUrl(request.getCancelUrl())
                .returnUrl(request.getReturnUrl())
                .buyerName(request.getBuyerName())
                .buyerEmail(request.getBuyerEmail())
                .expiredAt(request.getExpiredAt())
                .build();

        CreatePaymentLinkResponse payosResponse = payOS.paymentRequests().create(payosRequest);

        PaymentTransaction transaction = existingTransaction != null ? existingTransaction : new PaymentTransaction();
        transaction.setOrderCode(payosResponse.getOrderCode());
    transaction.setUserId(user.getId());
    transaction.setPackageCode(learningPackage.getPackageCode());
        transaction.setAmount(payosResponse.getAmount());
        transaction.setStatus(mapStatus(payosResponse.getStatus()));
        transaction.setPaymentLinkId(payosResponse.getPaymentLinkId());
        transaction.setCheckoutUrl(payosResponse.getCheckoutUrl());
        transaction.setQrCode(payosResponse.getQrCode());
        transaction.setBuyerName(request.getBuyerName());
        transaction.setBuyerEmail(request.getBuyerEmail());
        transaction.setPayosRequest(writeJson(payosRequest));
        transaction.setPayosResponse(writeJson(payosResponse));
        paymentTransactionRepository.save(transaction);

        return toResponse(transaction);
    }

    @Override
    public CheckoutResponseData getPaymentStatus(Long orderCode) {
        PaymentTransaction transaction = paymentTransactionRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment transaction not found"));
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

        if (transaction.getStatus() == PaymentStatus.PAID) {
            ensureEntitlement(transaction);
            return toResponse(transaction);
        }

        transaction.setStatus(PaymentStatus.PAID);
        transaction.setPaidAt(java.time.LocalDateTime.now());
        transaction.setWebhookPayload(writeJson(webhookBody));
        paymentTransactionRepository.save(transaction);

        ensureEntitlement(transaction);

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

    private java.time.LocalDateTime resolveEndAt(LearningPackage learningPackage, java.time.LocalDateTime paidAt) {
        if (learningPackage.getDurationDays() == null) {
            return null;
        }

        java.time.LocalDateTime startAt = paidAt != null ? paidAt : java.time.LocalDateTime.now();
        return startAt.plusDays(learningPackage.getDurationDays());
    }

    private String buildEntitlementMetadata(LearningPackage learningPackage, PaymentTransaction transaction) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "packageCode", learningPackage.getPackageCode(),
                    "gradeLevel", learningPackage.getGradeLevel(),
                    "paymentTransactionId", transaction.getId().toString(),
                    "paymentLinkId", transaction.getPaymentLinkId(),
                    "status", "ACTIVE"
            ));
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize entitlement metadata", e);
        }
    }

    private PaymentStatus mapStatus(vn.payos.model.v2.paymentRequests.PaymentLinkStatus payosStatus) {
        if (payosStatus == null) {
            return PaymentStatus.PENDING;
        }

        return switch (payosStatus) {
            case PAID -> PaymentStatus.PAID;
            case CANCELLED -> PaymentStatus.CANCELLED;
            case EXPIRED -> PaymentStatus.EXPIRED;
            default -> PaymentStatus.PENDING;
        };
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize payment payload", e);
        }
    }
}