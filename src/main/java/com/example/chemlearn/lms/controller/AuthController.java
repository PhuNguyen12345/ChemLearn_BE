package com.example.chemlearn.lms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.chemlearn.config.RequiredProductionDataSeeder;
import com.example.chemlearn.lms.dto.core.auth.AccessRequestCreateDTO;
import com.example.chemlearn.lms.dto.core.auth.AuthResponseDTO;
import com.example.chemlearn.lms.dto.core.auth.GoogleLoginRequestDTO;
import com.example.chemlearn.lms.dto.core.auth.InviteAcceptRequestDTO;
import com.example.chemlearn.lms.dto.core.auth.LoginRequestDTO;
import com.example.chemlearn.lms.dto.core.auth.OtpResendRequestDTO;
import com.example.chemlearn.lms.dto.core.auth.OtpVerifyRequestDTO;
import com.example.chemlearn.lms.dto.core.auth.RegisterRequestDTO;
import com.example.chemlearn.lms.service.AuthOnboardingService;
import com.example.chemlearn.lms.service.AuthService;
import com.example.chemlearn.lms.service.PasswordResetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor()
public class AuthController {

    private final AuthService authService;
    private final AuthOnboardingService onboardingService;
    private final PasswordResetService passwordResetService;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/deployment-info")
    public ResponseEntity<Map<String, Object>> deploymentInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("buildMarker", RequiredProductionDataSeeder.buildMarker());
        info.put("admin", jdbcTemplate.query("""
                        SELECT username, role, is_active, failed_login_attempts, lockout_until
                        FROM users
                        WHERE username = 'duckhisuu'
                        """,
                rs -> {
                    if (!rs.next()) {
                        return Map.of("exists", false);
                    }
                    Map<String, Object> admin = new LinkedHashMap<>();
                    admin.put("exists", true);
                    admin.put("username", rs.getString("username"));
                    admin.put("role", rs.getString("role"));
                    admin.put("isActive", rs.getBoolean("is_active"));
                    admin.put("failedLoginAttempts", rs.getObject("failed_login_attempts"));
                    admin.put("lockoutUntil", rs.getObject("lockout_until"));
                    return admin;
                }));
        info.put("premadeLabCount", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM lab WHERE type = 'PREMADE'",
                Integer.class
        ));
        info.put("premadeLabs", jdbcTemplate.query("""
                        SELECT title, category, difficulty
                        FROM lab
                        WHERE type = 'PREMADE'
                        ORDER BY title
                        """,
                (rs, rowNum) -> {
                    Map<String, Object> lab = new LinkedHashMap<>();
                    lab.put("title", rs.getString("title"));
                    lab.put("category", rs.getString("category"));
                    lab.put("difficulty", rs.getString("difficulty"));
                    return lab;
                }));
        info.put("latestFlyway", latestFlyway());
        return ResponseEntity.ok(info);
    }

    private Object latestFlyway() {
        try {
            return jdbcTemplate.query("""
                            SELECT version, description, success
                            FROM flyway_schema_history
                            ORDER BY installed_rank DESC
                            LIMIT 1
                            """,
                    rs -> {
                        if (!rs.next()) {
                            return Map.of("status", "empty");
                        }
                        Map<String, Object> flyway = new LinkedHashMap<>();
                        flyway.put("version", rs.getString("version"));
                        flyway.put("description", rs.getString("description"));
                        flyway.put("success", rs.getBoolean("success"));
                        return flyway;
                    });
        } catch (Exception ex) {
            return Map.of("status", "unavailable", "message", ex.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO dto) {
        if ("ROLE_TEACHER".equals(dto.getRole()) || "ROLE_PARENT".equals(dto.getRole())) {
            authService.registerTeacherParentPending(dto);
            return ResponseEntity.ok(Map.of("message", "Account request submitted for admin approval"));
        }

        authService.register(dto);
        return ResponseEntity.ok("Registered successfully");
    }

    @PostMapping("/register/otp")
    public ResponseEntity<?> registerWithOtp(@Valid @RequestBody RegisterRequestDTO dto) {
        authService.registerWithOtp(dto);
        return ResponseEntity.ok(Map.of("message", "OTP sent to email"));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody OtpVerifyRequestDTO dto) {
        authService.verifyOtpAndCreateAccount(dto);
        return ResponseEntity.ok(Map.of("message", "Account created successfully"));
    }

    @PostMapping("/otp/resend")
    public ResponseEntity<?> resendOtp(@Valid @RequestBody OtpResendRequestDTO dto) {
        authService.resendOtp(dto.getEmail());
        return ResponseEntity.ok(Map.of("message", "OTP resent to email"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponseDTO> googleLogin(
            @Valid @RequestBody GoogleLoginRequestDTO dto) {
        return ResponseEntity.ok(authService.loginWithGoogle(dto));
    }

    @PostMapping("/requests")
    public ResponseEntity<?> submitAccessRequest(
            @Valid @RequestBody AccessRequestCreateDTO dto) {
        onboardingService.submitAccessRequest(dto);
        return ResponseEntity.ok("Request submitted");
    }

    @PostMapping("/invites/accept")
    public ResponseEntity<?> acceptInvite(
            @Valid @RequestBody InviteAcceptRequestDTO dto) {
        onboardingService.acceptInvite(dto);
        return ResponseEntity.ok("Invite accepted");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        authService.logout(token);
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody java.util.Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("{\"message\":\"Email is required\"}");
        }
        passwordResetService.requestPasswordReset(email);
        return ResponseEntity.ok().body("{\"message\":\"Nếu email tồn tại trong hệ thống, chúng tôi đã gửi link đặt lại mật khẩu.\"}" );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody java.util.Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        if (token == null || newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body("{\"message\":\"Token và mật khẩu mới là bắt buộc (tối thiểu 6 ký tự).\"}");
        }
        passwordResetService.resetPassword(token, newPassword);
        return ResponseEntity.ok().body("{\"message\":\"Mật khẩu đã được đặt lại thành công!\"}" );
    }
}
