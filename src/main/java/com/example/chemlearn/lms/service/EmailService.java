package com.example.chemlearn.lms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:${spring.mail.username:no-reply@chemlearn.local}}")
    private String fromEmail;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Async
    public void sendLinkConfirmationEmail(String toEmail, String initiatorName, String token) {
        String confirmUrl = frontendUrl("/confirm-link?token=" + token);
        String subject = "ChemLearn - Yêu cầu liên kết tài khoản từ " + initiatorName;
        String body = "Xin chào,\n\n" +
                initiatorName + " đã gửi yêu cầu liên kết tài khoản với bạn trên ChemLearn.\n" +
                "Vui lòng nhấn vào đường link dưới đây để xác nhận liên kết:\n\n" +
                confirmUrl + "\n\n" +
                "Nếu bạn không biết người này, vui lòng bỏ qua email.\n" +
                "Trân trọng,\nĐội ngũ ChemLearn";

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {}", toEmail, e);
            // Don't throw - async, don't break the main flow
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String fullName, String token) {
        String resetUrl = frontendUrl("/reset-password?token=" + token);
        String subject = "ChemLearn - Yêu cầu đặt lại mật khẩu";
        String body = "Xin chào " + fullName + ",\n\n" +
                "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn trên ChemLearn.\n" +
                "Vui lòng nhấn vào đường link dưới đây để đặt lại mật khẩu (link có hiệu lực trong 30 phút):\n\n" +
                resetUrl + "\n\n" +
                "Nếu bạn không yêu cầu điều này, vui lòng bỏ qua email.\n" +
                "Trân trọng,\nĐội ngũ ChemLearn";

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}", toEmail, e);
        }
    }

    public void sendOtpEmail(String toEmail, String fullName, String otpCode) {
        String subject = "ChemLearn - Mã xác thực OTP";
        String body = "Xin chào " + fullName + ",\n\n" +
                "Mã OTP của bạn là: " + otpCode + "\n\n" +
                "Mã có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này với bất kỳ ai.\n\n" +
                "Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email.\n" +
                "Trân trọng,\nĐội ngũ ChemLearn";

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("OTP email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}", toEmail, e);
            throw new IllegalStateException("Failed to send OTP email", e);
        }
    }

    private String frontendUrl(String path) {
        String baseUrl = frontendBaseUrl == null ? "http://localhost:5173" : frontendBaseUrl.trim();
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + path;
    }
}
