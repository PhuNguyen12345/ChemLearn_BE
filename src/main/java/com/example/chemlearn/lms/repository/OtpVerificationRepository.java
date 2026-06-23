package com.example.chemlearn.lms.repository;

import com.example.chemlearn.core.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, UUID> {
    Optional<OtpVerification> findByEmailAndOtpCodeAndVerifiedFalse(String email, String otpCode);

    Optional<OtpVerification> findTopByEmailAndOtpCodeAndVerifiedFalseAndPendingRegistrationDataIsNullOrderByCreatedAtDesc(
            String email,
            String otpCode
    );

    Optional<OtpVerification> findTopByEmailOrderByCreatedAtDesc(String email);

    List<OtpVerification> findByEmailAndVerifiedFalse(String email);

    long countByEmailAndCreatedAtAfter(String email, Instant createdAt);

    void deleteByEmail(String email);
}
