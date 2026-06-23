package com.example.chemlearn.lms.service;

import com.example.chemlearn.core.entity.OtpVerification;
import com.example.chemlearn.lms.exception.CustomExceptions;
import com.example.chemlearn.lms.repository.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class OtpRateLimitService {

    private final OtpVerificationRepository otpVerificationRepository;

    @Value("${auth.otp.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    @Value("${auth.otp.max-per-hour:5}")
    private long maxPerHour;

    @Value("${auth.otp.max-per-day:10}")
    private long maxPerDay;

    public String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new CustomExceptions.BadRequestException("Email is required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public void assertCanSend(String email) {
        String normalizedEmail = normalizeEmail(email);
        Instant now = Instant.now();

        otpVerificationRepository.findTopByEmailOrderByCreatedAtDesc(normalizedEmail)
                .filter(otp -> otp.getCreatedAt() != null)
                .ifPresent(otp -> {
                    Instant nextAllowedAt = otp.getCreatedAt().plusSeconds(resendCooldownSeconds);
                    if (nextAllowedAt.isAfter(now)) {
                        long waitSeconds = Math.max(1, Duration.between(now, nextAllowedAt).toSeconds());
                        throw new CustomExceptions.BadRequestException(
                                "Vui lòng chờ " + waitSeconds + " giây trước khi yêu cầu OTP mới."
                        );
                    }
                });

        if (maxPerHour > 0 && otpVerificationRepository.countByEmailAndCreatedAtAfter(
                normalizedEmail,
                now.minus(Duration.ofHours(1))
        ) >= maxPerHour) {
            throw new CustomExceptions.BadRequestException("Bạn đã yêu cầu quá nhiều mã OTP trong 1 giờ. Vui lòng thử lại sau.");
        }

        if (maxPerDay > 0 && otpVerificationRepository.countByEmailAndCreatedAtAfter(
                normalizedEmail,
                now.minus(Duration.ofDays(1))
        ) >= maxPerDay) {
            throw new CustomExceptions.BadRequestException("Bạn đã yêu cầu quá nhiều mã OTP trong ngày. Vui lòng thử lại vào ngày mai.");
        }
    }

    public void invalidateOpenOtps(String email) {
        String normalizedEmail = normalizeEmail(email);
        List<OtpVerification> openOtps = otpVerificationRepository.findByEmailAndVerifiedFalse(normalizedEmail);
        if (openOtps.isEmpty()) {
            return;
        }
        openOtps.forEach(otp -> otp.setVerified(true));
        otpVerificationRepository.saveAll(openOtps);
    }
}
