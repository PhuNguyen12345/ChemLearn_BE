package com.example.chemlearn.lms.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final String VIETNAMESE_EMAIL_FONT_STACK = "'Segoe UI',Roboto,Arial,'Helvetica Neue',Helvetica,sans-serif";
    private static final String VIETNAMESE_EMAIL_FONT_STYLE = "font-family:" + VIETNAMESE_EMAIL_FONT_STACK + ";";

    private final JavaMailSender mailSender;
    private final AtomicInteger primarySentToday = new AtomicInteger();
    private volatile LocalDate primaryCounterDate = LocalDate.MIN;

    @Value("${app.mail.from:${spring.mail.username:no-reply@chemlearn.local}}")
    private String fromEmail;

    @Value("${app.mail.from-name:ChemLearn.vn}")
    private String fromName;

    @Value("${app.mail.reply-to:}")
    private String replyToEmail;

    @Value("${app.mail.fallback.enabled:false}")
    private boolean fallbackMailEnabled;

    @Value("${app.mail.fallback.host:smtp.gmail.com}")
    private String fallbackMailHost;

    @Value("${app.mail.fallback.port:587}")
    private int fallbackMailPort;

    @Value("${app.mail.fallback.username:}")
    private String fallbackMailUsername;

    @Value("${app.mail.fallback.password:}")
    private String fallbackMailPassword;

    @Value("${app.mail.fallback.from:}")
    private String fallbackMailFrom;

    @Value("${app.mail.fallback.from-name:${app.mail.from-name:ChemLearn.vn}}")
    private String fallbackMailFromName;

    @Value("${app.mail.fallback.reply-to:${app.mail.reply-to:}}")
    private String fallbackMailReplyTo;

    @Value("${app.mail.primary.daily-limit.enabled:true}")
    private boolean primaryDailyLimitEnabled;

    @Value("${app.mail.primary.daily-limit:280}")
    private int primaryDailyLimit;

    @Value("${app.mail.scheduler-zone:Asia/Ho_Chi_Minh}")
    private String mailSchedulerZone;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Value("${app.mail.mascot-url:}")
    private String mascotUrl;

    @Value("${app.mail.header-url:}")
    private String mailHeaderUrl;

    @Async
    public void sendLinkConfirmationEmail(String toEmail, String initiatorName, String token) {
        String confirmUrl = frontendUrl("/confirm-link?token=" + token);
        sendHtmlEmail(
                toEmail,
                "ChemLearn - Yêu cầu liên kết tài khoản",
                "Liên kết tài khoản",
                "Có yêu cầu liên kết mới",
                "Xin chào,",
                safeName(initiatorName) + " đã gửi yêu cầu liên kết tài khoản với bạn trên ChemLearn.",
                List.of(
                        "Xác nhận để phụ huynh và học sinh có thể theo dõi tiến độ học tập cùng nhau.",
                        "Nếu bạn không biết người gửi yêu cầu này, bạn có thể bỏ qua email."
                ),
                "Xác nhận liên kết",
                confirmUrl,
                "Liên kết này giúp ChemLearn bảo vệ quyền riêng tư của từng tài khoản.",
                false
        );
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String fullName, String token) {
        String resetUrl = frontendUrl("/reset-password?token=" + token);
        sendHtmlEmail(
                toEmail,
                "ChemLearn - Đặt lại mật khẩu",
                "Bảo mật tài khoản",
                "Đặt lại mật khẩu ChemLearn",
                greeting(fullName),
                "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.",
                List.of(
                        "Link đặt lại mật khẩu có hiệu lực trong 30 phút.",
                        "Nếu bạn không yêu cầu thao tác này, hãy bỏ qua email và mật khẩu hiện tại vẫn được giữ nguyên."
                ),
                "Đặt lại mật khẩu",
                resetUrl,
                "Đừng chia sẻ link này với người khác để giữ tài khoản an toàn.",
                false
        );
    }

    public void sendPasswordResetOtpEmail(String toEmail, String fullName, String otpCode, Instant expiresAt) {
        sendHtmlEmail(
                toEmail,
                "ChemLearn - Mã OTP đặt lại mật khẩu",
                "Bảo mật tài khoản",
                "Xác thực đặt lại mật khẩu",
                greeting(fullName),
                "Dùng mã OTP bên dưới để mở màn hình đổi mật khẩu trên ChemLearn.",
                List.of(
                        "Mã OTP: " + otpCode,
                        "Mã có hiệu lực trong 5 phút.",
                        "Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này."
                ),
                "Nhập OTP",
                frontendUrl("/forgot-password"),
                "ChemLearn không bao giờ yêu cầu bạn gửi lại OTP qua tin nhắn hoặc cuộc gọi.",
                true,
                expiresAt
        );
    }

    public void sendOtpEmail(String toEmail, String fullName, String otpCode) {
        sendOtpEmail(toEmail, fullName, otpCode, null);
    }

    public void sendOtpEmail(String toEmail, String fullName, String otpCode, Instant expiresAt) {
        sendHtmlEmail(
                toEmail,
                "ChemLearn - Mã xác thực OTP",
                "Xác thực email",
                "Mã OTP của bạn",
                greeting(fullName),
                "Dùng mã bên dưới để hoàn tất bước xác thực trên ChemLearn.",
                List.of(
                        "Mã OTP: " + otpCode,
                        "Mã có hiệu lực trong 5 phút.",
                        "Vui lòng không chia sẻ mã này với bất kỳ ai."
                ),
                "Quay lại ChemLearn",
                frontendUrl("/auth/register"),
                "Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email.",
                true,
                expiresAt
        );
    }

    @Async
    public void sendInviteEmail(String toEmail, String role, String token) {
        String inviteUrl = frontendUrl("/invite?token=" + token);
        sendHtmlEmail(
                toEmail,
                "ChemLearn - Lời mời tham gia hệ thống",
                "Lời mời mới",
                "Bạn được mời tham gia ChemLearn",
                "Xin chào,",
                "Quản trị viên đã gửi lời mời tạo tài khoản " + displayRole(role) + " trên ChemLearn.",
                List.of(
                        "Lời mời có hiệu lực trong 24 giờ.",
                        "Hoàn tất đăng ký để bắt đầu sử dụng lớp học, bài học và công cụ theo dõi tiến độ."
                ),
                "Nhận lời mời",
                inviteUrl,
                "Nếu bạn không mong đợi lời mời này, bạn có thể bỏ qua email.",
                false
        );
    }

    @Async
    public void sendNotificationEmail(
            String toEmail,
            String fullName,
            String subject,
            String eyebrow,
            String title,
            String intro,
            List<String> highlights,
            String ctaLabel,
            String ctaUrl
    ) {
        sendHtmlEmail(
                toEmail,
                subject,
                eyebrow,
                title,
                greeting(fullName),
                intro,
                highlights,
                ctaLabel,
                ctaUrl,
                "ChemLearn gửi email này để bạn không bỏ lỡ hoạt động học tập quan trọng.",
                false
        );
    }

    public String frontendUrl(String pathOrUrl) {
        if (pathOrUrl == null || pathOrUrl.isBlank()) {
            return normalizedFrontendBaseUrl();
        }
        String value = pathOrUrl.trim();
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }
        String path = value.startsWith("/") ? value : "/" + value;
        return normalizedFrontendBaseUrl() + path;
    }

    private void sendHtmlEmail(
            String toEmail,
            String subject,
            String eyebrow,
            String title,
            String greeting,
            String intro,
            List<String> highlights,
            String ctaLabel,
            String ctaUrl,
            String footerNote,
            boolean throwOnFailure
    ) {
        sendHtmlEmail(toEmail, subject, eyebrow, title, greeting, intro, highlights, ctaLabel, ctaUrl, footerNote, throwOnFailure, null);
    }

    private void sendHtmlEmail(
            String toEmail,
            String subject,
            String eyebrow,
            String title,
            String greeting,
            String intro,
            List<String> highlights,
            String ctaLabel,
            String ctaUrl,
            String footerNote,
            boolean throwOnFailure,
            Instant expiresAt
    ) {
        String plainText;
        String html;
        try {
            plainText = buildPlainText(title, greeting, intro, highlights, ctaLabel, ctaUrl, footerNote);
            html = buildHtml(eyebrow, title, greeting, intro, highlights, ctaLabel, ctaUrl, footerNote);
        } catch (Exception e) {
            log.error("Failed to build email content for {}", toEmail, e);
            if (throwOnFailure) {
                throw new IllegalStateException("Failed to send email", e);
            }
            return;
        }

        if (isExpired(expiresAt)) {
            String message = "Email expired before SMTP send";
            log.warn("{} for {}", message, toEmail);
            if (throwOnFailure) {
                throw new IllegalStateException(message);
            }
            return;
        }

        if (primaryDailyLimitReached() && fallbackConfigured()) {
            log.warn("Primary SMTP daily limit reached. Sending {} via fallback SMTP", toEmail);
            sendFallback(toEmail, subject, plainText, html, throwOnFailure, null);
            return;
        }

        try {
            sendWith(mailSender, toEmail, subject, plainText, html, fromEmail, fromName, replyToEmail);
            recordPrimarySend();
            log.info("Email sent to {} via primary SMTP", toEmail);
        } catch (Exception primaryException) {
            log.warn("Primary SMTP failed for {}. Trying fallback SMTP if configured. Reason: {}",
                    toEmail,
                    primaryException.getMessage());

            sendFallback(toEmail, subject, plainText, html, throwOnFailure, primaryException);
        }
    }

    private void sendWith(
            JavaMailSender sender,
            String toEmail,
            String subject,
            String plainText,
            String html,
            String mailFrom,
            String mailFromName,
            String mailReplyTo
    ) throws Exception {
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                message,
                true,
                StandardCharsets.UTF_8.name()
        );
        if (hasText(mailFromName)) {
            helper.setFrom(mailFrom, mailFromName);
        } else {
            helper.setFrom(mailFrom);
        }
        if (hasText(mailReplyTo)) {
            helper.setReplyTo(mailReplyTo);
        }
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(plainText, html);

        sender.send(message);
    }

    private void sendFallback(
            String toEmail,
            String subject,
            String plainText,
            String html,
            boolean throwOnFailure,
            Exception primaryException
    ) {
        if (!fallbackConfigured()) {
            log.error("Failed to send email to {} and fallback SMTP is not configured", toEmail, primaryException);
            if (throwOnFailure) {
                throw new IllegalStateException("Failed to send email", primaryException);
            }
            return;
        }

        try {
            sendWith(
                    createFallbackMailSender(),
                    toEmail,
                    subject,
                    plainText,
                    html,
                    fallbackFromEmail(),
                    fallbackFromName(),
                    fallbackReplyToEmail()
            );
            log.info("Email sent to {} via fallback SMTP", toEmail);
        } catch (Exception fallbackException) {
            log.error("Fallback SMTP also failed for {}", toEmail, fallbackException);
            if (throwOnFailure) {
                throw new IllegalStateException("Failed to send email", fallbackException);
            }
        }
    }

    private JavaMailSender createFallbackMailSender() {
        JavaMailSenderImpl fallbackSender = new JavaMailSenderImpl();
        fallbackSender.setHost(fallbackMailHost);
        fallbackSender.setPort(fallbackMailPort);
        fallbackSender.setUsername(fallbackMailUsername);
        fallbackSender.setPassword(fallbackMailPassword);

        Properties properties = fallbackSender.getJavaMailProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");

        return fallbackSender;
    }

    private boolean fallbackConfigured() {
        return fallbackMailEnabled
                && hasText(fallbackMailHost)
                && fallbackMailPort > 0
                && hasText(fallbackMailUsername)
                && hasText(fallbackMailPassword);
    }

    private String fallbackFromEmail() {
        return hasText(fallbackMailFrom) ? fallbackMailFrom.trim() : fallbackMailUsername.trim();
    }

    private String fallbackFromName() {
        return hasText(fallbackMailFromName) ? fallbackMailFromName.trim() : fromName;
    }

    private String fallbackReplyToEmail() {
        return hasText(fallbackMailReplyTo) ? fallbackMailReplyTo.trim() : replyToEmail;
    }

    private boolean isExpired(Instant expiresAt) {
        return expiresAt != null && !Instant.now().isBefore(expiresAt);
    }

    private boolean primaryDailyLimitReached() {
        if (!primaryDailyLimitEnabled || primaryDailyLimit <= 0) {
            return false;
        }
        resetPrimaryCounterIfNeeded();
        return primarySentToday.get() >= primaryDailyLimit;
    }

    private void recordPrimarySend() {
        if (!primaryDailyLimitEnabled || primaryDailyLimit <= 0) {
            return;
        }
        resetPrimaryCounterIfNeeded();
        primarySentToday.incrementAndGet();
    }

    private void resetPrimaryCounterIfNeeded() {
        LocalDate today = LocalDate.now(resolveMailZone());
        if (today.equals(primaryCounterDate)) {
            return;
        }
        synchronized (this) {
            if (!today.equals(primaryCounterDate)) {
                primaryCounterDate = today;
                primarySentToday.set(0);
            }
        }
    }

    private ZoneId resolveMailZone() {
        try {
            return ZoneId.of(mailSchedulerZone);
        } catch (Exception ex) {
            return ZoneId.of("Asia/Ho_Chi_Minh");
        }
    }

    private String buildHtml(
            String eyebrow,
            String title,
            String greeting,
            String intro,
            List<String> highlights,
            String ctaLabel,
            String ctaUrl,
            String footerNote
    ) {
        if (modernEmailTemplateEnabled()) {
            return buildCampaignHtml(eyebrow, title, greeting, intro, highlights, ctaLabel, ctaUrl, footerNote);
        }

        String escapedTitle = escapeHtml(title);
        String escapedGreeting = escapeHtml(greeting);
        String escapedIntro = escapeHtml(intro);
        String introHtml = escapeHtmlWithLineBreaks(intro);
        String escapedEyebrow = escapeHtml(eyebrow);
        String safeCtaUrl = escapeHtml(frontendUrl(ctaUrl));
        String escapedCtaLabel = escapeHtml(hasText(ctaLabel) ? ctaLabel : "Mở ChemLearn");
        String escapedMascotUrl = escapeHtml(resolveMascotUrl());
        String escapedFooter = escapeHtml(hasText(footerNote)
                ? footerNote
                : "ChemLearn gửi email này để đồng hành cùng hành trình học của bạn.");

        List<String> safeHighlights = safeList(highlights);
        if (safeHighlights.isEmpty()) {
            safeHighlights = List.of(
                    "Mở ChemLearn để tiếp tục hành trình học hôm nay.",
                    "Bi sẽ nhắc bạn từng bước nhỏ để việc học nhẹ hơn."
            );
        }

        StringBuilder highlightHtml = new StringBuilder();
        for (String highlight : safeHighlights) {
            highlightHtml.append("""
                    <tr>
                      <td style="padding:8px 0;">
                        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0">
                          <tr>
                            <td width="28" valign="top" style="__FONT_STYLE__font-size:18px;line-height:26px;color:#14b8a6;">•</td>
                            <td style="__FONT_STYLE__font-size:15px;line-height:24px;color:#334155;">%s</td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                    """.formatted(escapeHtml(highlight)));
        }

        String html = """
                <!doctype html>
                <html lang="vi">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <meta name="color-scheme" content="light">
                  <meta name="supported-color-schemes" content="light">
                  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
                  <title>%s</title>
                  <style>
                    :root { color-scheme: light; supported-color-schemes: light; }
                    body, table, td, div, p, h1, a { font-family: __FONT_STACK__ !important; }
                  </style>
                </head>
                <body style="margin:0;padding:0;background:#111114;__FONT_STYLE__color:#f8fafc;-webkit-text-size-adjust:100%%;text-size-adjust:100%%;">
                  <div style="display:none;max-height:0;overflow:hidden;color:transparent;">%s</div>
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#111114;padding:28px 12px;__FONT_STYLE__">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:640px;background:#1d1d22;border-radius:0;overflow:hidden;border:1px solid #2c2c33;__FONT_STYLE__">
                          <tr>
                            <td style="background:#2b1065;background-image:linear-gradient(180deg,#35117f 0%%,#211733 100%%);padding:36px 28px 24px;text-align:center;__FONT_STYLE__">
                              <div style="__FONT_STYLE__font-size:34px;line-height:40px;color:#a7f3d0;font-weight:900;letter-spacing:0;margin-bottom:16px;">ChemLearn</div>
                              <img src="%s" width="260" alt="Bi ChemLearn" style="display:block;margin:0 auto;max-width:260px;width:72%%;height:auto;border:0;">
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:30px 32px 10px;__FONT_STYLE__">
                              <p style="margin:0 0 14px;__FONT_STYLE__font-size:17px;line-height:27px;color:#0f172a;font-weight:700;">%s</p>
                              <p style="margin:0 0 18px;__FONT_STYLE__font-size:16px;line-height:26px;color:#334155;">%s</p>
                              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#f0fdfa;border:1px solid #99f6e4;border-radius:18px;padding:12px 18px;margin:20px 0;__FONT_STYLE__">
                                %s
                              </table>
                              <div style="text-align:center;padding:10px 0 24px;">
                                <a href="%s" style="display:inline-block;background:#0f766e;color:#ffffff;text-decoration:none;__FONT_STYLE__font-weight:700;font-size:16px;line-height:22px;padding:14px 24px;border-radius:999px;">%s</a>
                              </div>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:18px 32px 28px;background:#f8fafc;border-top:1px solid #e2e8f0;__FONT_STYLE__">
                              <p style="margin:0;__FONT_STYLE__font-size:13px;line-height:21px;color:#64748b;text-align:center;">%s</p>
                              <p style="margin:10px 0 0;__FONT_STYLE__font-size:12px;line-height:19px;color:#94a3b8;text-align:center;">© ChemLearn</p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(
                escapedTitle,
                escapedIntro,
                escapedMascotUrl,
                escapedEyebrow,
                escapedTitle,
                escapedGreeting,
                introHtml,
                highlightHtml,
                safeCtaUrl,
                escapedCtaLabel,
                escapedFooter
        );
        return html
                .replace("__FONT_STACK__", VIETNAMESE_EMAIL_FONT_STACK)
                .replace("__FONT_STYLE__", VIETNAMESE_EMAIL_FONT_STYLE);
    }

    private String buildCampaignHtml(
            String eyebrow,
            String title,
            String greeting,
            String intro,
            List<String> highlights,
            String ctaLabel,
            String ctaUrl,
            String footerNote
    ) {
        String escapedTitle = escapeHtml(title);
        String escapedGreeting = escapeHtml(greeting);
        String escapedIntro = escapeHtml(intro);
        String introHtml = escapeHtmlWithLineBreaks(intro);
        String escapedEyebrow = escapeHtml(hasText(eyebrow) ? eyebrow : "ChemLearn reminder");
        String safeCtaUrl = escapeHtml(frontendUrl(ctaUrl));
        String escapedCtaLabel = escapeHtml(hasText(ctaLabel) ? ctaLabel : "Mở ChemLearn");
        String escapedHeaderUrl = escapeHtml(resolveMailHeaderUrl());
        String escapedFooter = escapeHtml(hasText(footerNote)
                ? footerNote
                : "ChemLearn gửi email này để đồng hành cùng hành trình học của bạn.");

        List<String> safeHighlights = safeList(highlights);
        if (safeHighlights.isEmpty()) {
            safeHighlights = List.of(
                    "Mở ChemLearn để tiếp tục hành trình học hôm nay.",
                    "Bi sẽ nhắc bạn từng bước nhỏ để việc học nhẹ hơn."
            );
        }

        StringBuilder actionRows = new StringBuilder();
        String[] icons = {"🔔", "🧪", "🔥", "⭐", "💬"};
        int index = 0;
        for (String highlight : safeHighlights) {
            String icon = icons[index % icons.length];
            actionRows.append("""
                    <tr>
                      <td style="padding:0 0 18px;">
                        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="border-collapse:collapse;">
                          <tr>
                            <td width="58" valign="top">
                              <div style="width:48px;height:48px;border-radius:14px;background:#efe7ff;text-align:center;line-height:48px;__FONT_STYLE__font-size:24px;">%s</div>
                            </td>
                            <td valign="middle" style="padding-left:10px;">
                              <div style="__FONT_STYLE__font-size:17px;line-height:24px;color:#1f2937;font-weight:800;letter-spacing:0;">%s</div>
                              <div style="__FONT_STYLE__font-size:14px;line-height:22px;color:#6b6280;margin-top:3px;">Bi sẽ đồng hành cùng bạn từng bước.</div>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                    """.formatted(icon, escapeHtml(highlight)));
            index++;
        }

        String html = """
                <!doctype html>
                <html lang="vi">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <meta name="color-scheme" content="light">
                  <meta name="supported-color-schemes" content="light">
                  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
                  <title>%s</title>
                  <style>
                    :root { color-scheme: light; supported-color-schemes: light; }
                    body, table, td, div, p, h1, h2, a { font-family: __FONT_STACK__ !important; }
                  </style>
                </head>
                <body style="margin:0;padding:0;background:#f4f2fb;__FONT_STYLE__color:#1f2937;-webkit-text-size-adjust:100%%;text-size-adjust:100%%;">
                  <div style="display:none;max-height:0;overflow:hidden;color:transparent;">%s</div>
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f2fb;padding:22px 10px;__FONT_STYLE__">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:640px;background:#ffffff;border:1px solid #e3ddf5;border-radius:0;overflow:hidden;__FONT_STYLE__">
                          <tr>
                            <td style="background:#050316;padding:0;line-height:0;font-size:0;__FONT_STYLE__">
                              <img src="%s" width="640" alt="ChemLearn" style="display:block;width:100%%;max-width:640px;height:auto;border:0;outline:none;text-decoration:none;">
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:30px 28px 18px;text-align:left;__FONT_STYLE__">
                              <div style="__FONT_STYLE__font-size:12px;font-weight:900;letter-spacing:1.5px;text-transform:uppercase;color:#1f7a2e;margin-bottom:14px;">%s</div>
                              <h1 style="margin:0 0 18px;__FONT_STYLE__font-size:32px;line-height:40px;color:#2b2835;font-weight:900;letter-spacing:0;">%s</h1>
                              <p style="margin:0 0 10px;__FONT_STYLE__font-size:17px;line-height:28px;color:#34303d;font-weight:800;">%s</p>
                              <p style="margin:0 0 24px;__FONT_STYLE__font-size:16px;line-height:27px;color:#665f7c;">%s</p>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:8px 28px;__FONT_STYLE__">
                              <div style="height:1px;background:#e6e0f4;line-height:1px;font-size:1px;">&nbsp;</div>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:26px 28px 16px;text-align:left;__FONT_STYLE__">
                              <h2 style="margin:0 0 24px;__FONT_STYLE__font-size:28px;line-height:36px;color:#2b2835;font-weight:900;letter-spacing:0;">Cùng Bi làm ngay nhé</h2>
                              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="border-collapse:collapse;">
                                %s
                              </table>
                              <div style="text-align:left;padding:8px 0 28px;">
                                <a href="%s" style="display:inline-block;background:#78f51d;color:#07120d;text-decoration:none;__FONT_STYLE__font-weight:900;font-size:16px;line-height:22px;letter-spacing:.5px;text-transform:uppercase;padding:15px 34px;border-radius:10px;border-bottom:4px solid #48ad09;">%s</a>
                              </div>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:22px 28px 30px;background:#f7f5fc;border-top:1px solid #e6e0f4;__FONT_STYLE__">
                              <p style="margin:0;__FONT_STYLE__font-size:13px;line-height:22px;color:#746c87;text-align:left;">%s</p>
                              <p style="margin:16px 0 0;__FONT_STYLE__font-size:12px;line-height:19px;color:#8a8399;text-align:left;">ChemLearn - Học hóa dễ hiểu, nhớ lâu, đạt điểm cao</p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(
                escapedTitle,
                escapedIntro,
                escapedHeaderUrl,
                escapedEyebrow,
                escapedTitle,
                escapedGreeting,
                introHtml,
                actionRows,
                safeCtaUrl,
                escapedCtaLabel,
                escapedFooter
        );
        return html
                .replace("__FONT_STACK__", VIETNAMESE_EMAIL_FONT_STACK)
                .replace("__FONT_STYLE__", VIETNAMESE_EMAIL_FONT_STYLE);
    }

    private boolean modernEmailTemplateEnabled() {
        return true;
    }

    private String buildPlainText(
            String title,
            String greeting,
            String intro,
            List<String> highlights,
            String ctaLabel,
            String ctaUrl,
            String footerNote
    ) {
        StringBuilder text = new StringBuilder();
        text.append(title).append("\n\n")
                .append(greeting).append("\n")
                .append(intro).append("\n\n");
        for (String highlight : safeList(highlights)) {
            text.append("- ").append(highlight).append("\n");
        }
        text.append("\n").append(ctaLabel).append(": ").append(frontendUrl(ctaUrl)).append("\n\n")
                .append(footerNote).append("\nChemLearn");
        return text.toString();
    }

    private String normalizedFrontendBaseUrl() {
        String baseUrl = frontendBaseUrl == null || frontendBaseUrl.isBlank()
                ? "http://localhost:5173"
                : frontendBaseUrl.trim();
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    private String resolveMascotUrl() {
        if (hasText(mascotUrl)) {
            String value = mascotUrl.trim();
            if (value.startsWith("http://") || value.startsWith("https://")) {
                return value;
            }
            return frontendUrl(value);
        }
        return frontendUrl("/bi-companion.png");
    }

    private String resolveMailHeaderUrl() {
        if (hasText(mailHeaderUrl)) {
            String value = mailHeaderUrl.trim();
            if (value.startsWith("http://") || value.startsWith("https://")) {
                return value;
            }
            return frontendUrl(value);
        }
        return frontendUrl("/chemlearn-mail-header.png");
    }

    private String greeting(String fullName) {
        return "Xin chào " + safeName(fullName) + ",";
    }

    private String safeName(String value) {
        return value == null || value.isBlank() ? "bạn" : value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private List<String> safeList(List<String> values) {
        return values == null ? List.of() : values.stream()
                .filter(value -> value != null && !value.isBlank())
                .toList();
    }

    private String displayRole(String role) {
        if ("ROLE_TEACHER".equals(role)) {
            return "giáo viên";
        }
        if ("ROLE_PARENT".equals(role)) {
            return "phụ huynh";
        }
        if ("ROLE_STUDENT".equals(role)) {
            return "học sinh";
        }
        return "người dùng";
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String escapeHtmlWithLineBreaks(String value) {
        return escapeHtml(value).replace("\r\n", "\n").replace("\r", "\n").replace("\n", "<br>");
    }
}
