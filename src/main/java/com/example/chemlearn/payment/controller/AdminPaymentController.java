package com.example.chemlearn.payment.controller;

import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.lms.repository.UserRepository;
import com.example.chemlearn.payment.dto.LearningPackageResponse;
import com.example.chemlearn.payment.dto.UserPackageEntitlementResponse;
import com.example.chemlearn.payment.entity.LearningPackage;
import com.example.chemlearn.payment.entity.PaymentTransaction;
import com.example.chemlearn.payment.entity.UserPackageEntitlement;
import com.example.chemlearn.payment.enums.EntitlementStatus;
import com.example.chemlearn.payment.enums.PaymentStatus;
import com.example.chemlearn.payment.repository.LearningPackageRepository;
import com.example.chemlearn.payment.repository.PaymentTransactionRepository;
import com.example.chemlearn.payment.repository.UserPackageEntitlementRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final LearningPackageRepository learningPackageRepository;
    private final UserPackageEntitlementRepository entitlementRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final UserRepository userRepository;

    @GetMapping("/packages")
    public List<LearningPackageResponse> getPackages() {
        return learningPackageRepository.findAll().stream()
                .sorted(Comparator
                        .comparing(LearningPackage::getGradeLevel, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(LearningPackage::getPackageCode, Comparator.nullsLast(String::compareTo)))
                .map(this::toPackageResponse)
                .toList();
    }

    @PostMapping("/packages")
    @Transactional
    public ResponseEntity<LearningPackageResponse> createPackage(@Valid @RequestBody PackageRequest request) {
        String packageCode = normalizePackageCode(request.getPackageCode());
        learningPackageRepository.findByPackageCode(packageCode).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Package code already exists");
        });

        LearningPackage learningPackage = new LearningPackage();
        learningPackage.setPackageCode(packageCode);
        applyPackageRequest(learningPackage, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(toPackageResponse(learningPackageRepository.save(learningPackage)));
    }

    @PutMapping("/packages/{packageCode}")
    @Transactional
    public LearningPackageResponse updatePackage(
            @PathVariable String packageCode,
            @Valid @RequestBody PackageRequest request
    ) {
        LearningPackage learningPackage = findPackage(packageCode);
        applyPackageRequest(learningPackage, request);
        return toPackageResponse(learningPackageRepository.save(learningPackage));
    }

    @DeleteMapping("/packages/{packageCode}")
    @Transactional
    public ResponseEntity<Void> deletePackage(@PathVariable String packageCode) {
        LearningPackage learningPackage = findPackage(packageCode);
        entitlementRepository.deleteAll(entitlementRepository.findByPackageCode(learningPackage.getPackageCode()));
        entitlementRepository.flush();
        learningPackageRepository.delete(learningPackage);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/entitlements")
    public List<UserPackageEntitlementResponse> getEntitlements(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String email
    ) {
        User student = findStudent(userId, email);
        return entitlementRepository.findByUserIdAndStatusIn(
                        student.getId(),
                        List.of(EntitlementStatus.ACTIVE, EntitlementStatus.PENDING, EntitlementStatus.CANCELLED, EntitlementStatus.EXPIRED)
                ).stream()
                .map(this::toEntitlementResponse)
                .toList();
    }

    @PostMapping("/entitlements/grants")
    @Transactional
    public UserPackageEntitlementResponse grantPackage(@Valid @RequestBody GrantRequest request) {
        User student = findStudent(request.getUserId(), request.getEmail());
        LearningPackage learningPackage = findPackage(request.getPackageCode());

        entitlementRepository.findByUserIdAndPackageCodeAndStatusIn(
                student.getId(),
                learningPackage.getPackageCode(),
                List.of(EntitlementStatus.ACTIVE, EntitlementStatus.PENDING)
        ).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Student already has this package");
        });

        LocalDateTime now = LocalDateTime.now();
        Integer durationDays = request.getDurationDays() != null
                ? request.getDurationDays()
                : learningPackage.getDurationDays();

        PaymentTransaction transaction = PaymentTransaction.builder()
                .orderCode(nextInternalOrderCode())
                .userId(student.getId())
                .packageCode(learningPackage.getPackageCode())
                .amount(0L)
                .status(PaymentStatus.PAID)
                .paidAt(now)
                .payosRequest("{\"source\":\"admin-grant\"}")
                .payosResponse("{\"note\":\"" + safeJsonText(request.getNote()) + "\"}")
                .build();
        PaymentTransaction savedTransaction = paymentTransactionRepository.save(transaction);

        UserPackageEntitlement entitlement = UserPackageEntitlement.builder()
                .userId(student.getId())
                .packageCode(learningPackage.getPackageCode())
                .paymentTransactionId(savedTransaction.getId())
                .status(EntitlementStatus.ACTIVE)
                .startAt(now)
                .endAt(durationDays == null ? null : now.plusDays(durationDays))
                .metadataJson("{\"source\":\"admin-grant\",\"note\":\"" + safeJsonText(request.getNote()) + "\"}")
                .build();

        return toEntitlementResponse(entitlementRepository.save(entitlement));
    }

    @PatchMapping("/entitlements/{entitlementId}/revoke")
    @Transactional
    public UserPackageEntitlementResponse revokeEntitlement(
            @PathVariable UUID entitlementId,
            @RequestBody(required = false) RevokeRequest request
    ) {
        UserPackageEntitlement entitlement = entitlementRepository.findById(entitlementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entitlement not found"));

        entitlement.setStatus(EntitlementStatus.CANCELLED);
        entitlement.setCancelledAt(LocalDateTime.now());
        entitlement.setCancellationReason(request == null || request.getReason() == null || request.getReason().isBlank()
                ? "Revoked by admin"
                : request.getReason().trim());

        return toEntitlementResponse(entitlementRepository.save(entitlement));
    }

    private LearningPackage findPackage(String packageCode) {
        return learningPackageRepository.findByPackageCode(normalizePackageCode(packageCode))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Package not found"));
    }

    private User findStudent(UUID userId, String email) {
        if (email != null && !email.isBlank()) {
            return userRepository.findByEmailIgnoreCase(email.trim())
                    .filter(user -> UserRole.ROLE_STUDENT.equals(user.getRole()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found for email"));
        }

        if (userId != null) {
            return userRepository.findByIdAndRole(userId, UserRole.ROLE_STUDENT)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student email is required");
    }

    private void applyPackageRequest(LearningPackage learningPackage, PackageRequest request) {
        learningPackage.setPackageName(request.getPackageName().trim());
        learningPackage.setGradeLevel(request.getGradeLevel());
        learningPackage.setBasePrice(request.getBasePrice());
        learningPackage.setDurationDays(request.getDurationDays());
        learningPackage.setDescription(blankToNull(request.getDescription()));
        learningPackage.setBenefitsJson(blankToNull(request.getBenefitsJson()));
        learningPackage.setIsActive(request.getIsActive() == null || request.getIsActive());
    }

    private String normalizePackageCode(String packageCode) {
        if (packageCode == null || packageCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Package code is required");
        }
        return packageCode.trim().toUpperCase();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Long nextInternalOrderCode() {
        long orderCode;
        do {
            orderCode = -ThreadLocalRandom.current().nextLong(1_000_000_000L, Long.MAX_VALUE);
        } while (paymentTransactionRepository.existsByOrderCode(orderCode));
        return orderCode;
    }

    private String safeJsonText(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
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
                .status(entitlement.getStatus() == null ? null : entitlement.getStatus().name())
                .startAt(entitlement.getStartAt())
                .endAt(entitlement.getEndAt())
                .cancelledAt(entitlement.getCancelledAt())
                .cancellationReason(entitlement.getCancellationReason())
                .metadataJson(entitlement.getMetadataJson())
                .build();
    }

    @Data
    public static class PackageRequest {
        private String packageCode;

        @NotBlank(message = "Package name is required")
        private String packageName;

        private Integer gradeLevel;
        private Long basePrice;
        private Integer durationDays;
        private String description;
        private String benefitsJson;
        private Boolean isActive;
    }

    @Data
    public static class GrantRequest {
        private UUID userId;
        private String email;

        @NotBlank(message = "Package code is required")
        private String packageCode;

        private Integer durationDays;
        private String note;
    }

    @Data
    public static class RevokeRequest {
        private String reason;
    }
}
