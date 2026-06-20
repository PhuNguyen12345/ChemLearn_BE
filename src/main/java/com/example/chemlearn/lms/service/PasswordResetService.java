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
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int RESET_TOKEN_EXPIRY_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            log.warn("Password reset requested for unknown email: {}", email);
            return;
        }

        otpVerificationRepository.deleteByEmail(email);
        tokenRepository.deleteByUserId(user.getId());

        String otpCode = generateOtp();
        OtpVerification otp = new OtpVerification();
        otp.setEmail(email);
        otp.setOtpCode(otpCode);
        otp.setPendingRegistrationData(null);
        otp.setCreatedAt(Instant.now());
        otp.setExpiresAt(Instant.now().plus(Duration.ofMinutes(OTP_EXPIRY_MINUTES)));
        otp.setVerified(false);
        otpVerificationRepository.save(otp);

        emailService.sendPasswordResetOtpEmail(email, user.getFullName(), otpCode);
    }

    @Transactional
    public String verifyPasswordResetOtp(String email, String otpCode) {
        if (email == null || email.isBlank() || otpCode == null || otpCode.isBlank()) {
            throw new CustomExceptions.BadRequestException("Email và mã OTP là bắt buộc.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomExceptions.BadRequestException("Mã OTP không hợp lệ hoặc đã hết hạn."));

        OtpVerification otp = otpVerificationRepository
                .findTopByEmailAndOtpCodeAndVerifiedFalseAndPendingRegistrationDataIsNullOrderByCreatedAtDesc(email, otpCode)
                .orElseThrow(() -> new CustomExceptions.BadRequestException("Mã OTP không hợp lệ hoặc đã hết hạn."));

        if (otp.getExpiresAt().isBefore(Instant.now())) {
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
