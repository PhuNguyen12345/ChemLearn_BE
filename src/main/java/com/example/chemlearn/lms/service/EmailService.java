package com.example.chemlearn.lms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendLinkConfirmationEmail(String toEmail, String initiatorName, String token) {
        String confirmUrl = "http://localhost:5173/confirm-link?token=" + token;
        String subject = "ChemLearn - Yêu cầu liên kết tài khoản từ " + initiatorName;
        String body = "Xin chào,\n\n" +
                initiatorName + " đã gửi yêu cầu liên kết tài khoản với bạn trên ChemLearn.\n" +
                "Vui lòng nhấn vào đường link dưới đây để xác nhận liên kết:\n\n" +
                confirmUrl + "\n\n" +
                "Nếu bạn không biết người này, vui lòng bỏ qua email.\n" +
                "Trân trọng,\nĐội ngũ ChemLearn";

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("phamduchieu1407@gmail.com");
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
}
