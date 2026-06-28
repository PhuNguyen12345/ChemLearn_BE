package com.example.chemlearn.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AdminPaidStudentSubscriptionResponse {
    private UUID entitlementId;
    private String entitlementStatus;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private String metadataJson;
    private LocalDateTime entitlementCreatedAt;
    private LocalDateTime entitlementUpdatedAt;

    private UUID studentId;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String gender;
    private String avatarUrl;
    private Boolean accountActive;
    private Instant accountCreatedAt;

    private Integer gradeLevel;
    private Integer currentGrade;
    private Integer targetGraduationYear;
    private String schoolName;
    private Integer totalPoints;
    private Integer experience;
    private Integer currentStreak;
    private Integer coins;
    private Integer pvpWins;
    private LocalDate lastActiveDate;

    private String packageCode;
    private String packageName;
    private Integer packageGradeLevel;
    private String packageDescription;
    private Long packageBasePrice;
    private Integer packageDurationDays;
    private String packageBenefitsJson;

    private UUID paymentTransactionId;
    private Long orderCode;
    private Long amount;
    private String paymentStatus;
    private String paymentLinkId;
    private String buyerName;
    private String buyerEmail;
    private LocalDateTime paidAt;
    private LocalDateTime transactionCreatedAt;
}
