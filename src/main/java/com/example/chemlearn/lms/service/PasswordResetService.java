package com.example.chemlearn.lms.service;

import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.lms.entity.PasswordResetToken;
import com.example.chemlearn.lms.repository.PasswordResetTokenRepository;
import com.example.chemlearn.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void requestPasswordReset(String email) {
        // Always return without error to prevent email enumeration attacks
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            log.warn("Password reset requested for unknown email: {}", email);
            return; // silently ignore
        }

        // Delete any existing token for this user
        tokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(30, ChronoUnit.MINUTES);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .userId(user.getId())
                .token(token)
                .expiresAt(expiresAt)
                .used(false)
                .build();

        tokenRepository.save(resetToken);
        emailService.sendPasswordResetEmail(email, user.getFullName(), token);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn."));

        if (resetToken.isUsed()) {
            throw new RuntimeException("Link này đã được sử dụng. Vui lòng yêu cầu link mới.");
        }

        if (Instant.now().isAfter(resetToken.getExpiresAt())) {
            throw new RuntimeException("Link đặt lại mật khẩu đã hết hạn (30 phút). Vui lòng yêu cầu lại.");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Password reset successful for user: {}", user.getEmail());
    }
}
