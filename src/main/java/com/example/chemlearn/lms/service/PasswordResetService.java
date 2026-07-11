package com.example.chemlearn.lms.service;

import com.example.chemlearn.core.entity.OtpVerification;
import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.lms.entity.PasswordResetToken;
import com.example.chemlearn.lms.exception.CustomExceptions;
import com.example.chemlearn.lms.repository.OtpVerificationRepository;
import com.example.chemlearn.lms.repository.PasswordResetTokenRepository;
import com.example.chemlearn.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int RESET_TOKEN_EXPIRY_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final EmailService emailService;
    private final OtpRateLimitService otpRateLimitService;
    private final PasswordEncoder passwordEncoder;

    @Value("${auth.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    @Transactional
    public void requestPasswordReset(String email) {
        String normalizedEmail = otpRateLimitService.normalizeEmail(email);
        User user = userRepository.findByEmail(normalizedEmail).orElse(null);
        if (user == null) {
            log.warn("Password reset requested for unknown email: {}", normalizedEmail);
            return;
        }
        otpRateLimitService.assertCanSend(normalizedEmail);

        otpRateLimitService.invalidateOpenOtps(normalizedEmail);
        tokenRepository.deleteByUserId(user.getId());

        String otpCode = generateOtp();
        Instant now = Instant.now();
        OtpVerification otp = new OtpVerification();
        otp.setEmail(normalizedEmail);
        otp.setOtpCode(otpCode);
        otp.setPendingRegistrationData(null);
        otp.setCreatedAt(now);
        otp.setExpiresAt(now.plus(Duration.ofMinutes(otpExpiryMinutes)));
        otp.setVerified(false);
        otpVerificationRepository.save(otp);

        emailService.sendPasswordResetOtpEmail(normalizedEmail, user.getFullName(), otpCode, otp.getExpiresAt());
    }

    @Transactional
    public String verifyPasswordResetOtp(String email, String otpCode) {
        if (email == null || email.isBlank() || otpCode == null || otpCode.isBlank()) {
            throw new CustomExceptions.BadRequestException("Email và mã OTP là bắt buộc.");
        }

        String normalizedEmail = otpRateLimitService.normalizeEmail(email);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new CustomExceptions.BadRequestException("Mã OTP không hợp lệ hoặc đã hết hạn."));

        OtpVerification otp = otpVerificationRepository
                .findTopByEmailAndOtpCodeAndVerifiedFalseAndPendingRegistrationDataIsNullOrderByCreatedAtDesc(normalizedEmail, otpCode)
                .orElseThrow(() -> new CustomExceptions.BadRequestException("Mã OTP không hợp lệ hoặc đã hết hạn."));

        if (otp.getExpiresAt() == null || otp.getExpiresAt().isBefore(Instant.now())) {
            throw new CustomExceptions.BadRequestException("Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới.");
        }

        otp.setVerified(true);
        otpVerificationRepository.save(otp);

        tokenRepository.deleteByUserId(user.getId());
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(RESET_TOKEN_EXPIRY_MINUTES, ChronoUnit.MINUTES);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .userId(user.getId())
                .token(token)
                .expiresAt(expiresAt)
                .used(false)
                .build();
        tokenRepository.save(resetToken);

        return token;
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Phiên đặt lại mật khẩu không hợp lệ hoặc đã hết hạn."));

        if (resetToken.isUsed()) {
            throw new RuntimeException("Phiên đặt lại mật khẩu này đã được sử dụng. Vui lòng yêu cầu mã OTP mới.");
        }

        if (Instant.now().isAfter(resetToken.getExpiresAt())) {
            throw new RuntimeException("Phiên đặt lại mật khẩu đã hết hạn (30 phút). Vui lòng yêu cầu lại.");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Password reset successful for user: {}", user.getEmail());
    }

    private String generateOtp() {
        int otp = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(otp);
    }
}
