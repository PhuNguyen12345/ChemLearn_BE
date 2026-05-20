package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.core.auth.AuthResponseDTO;
import com.example.chemlearn.lms.dto.core.auth.AccessRequestCreateDTO;
import com.example.chemlearn.lms.dto.core.auth.GoogleLoginRequestDTO;
import com.example.chemlearn.lms.dto.core.auth.InviteAcceptRequestDTO;
import com.example.chemlearn.lms.dto.core.auth.LoginRequestDTO;
import com.example.chemlearn.lms.dto.core.auth.RegisterRequestDTO;
import com.example.chemlearn.lms.service.AuthService;
import com.example.chemlearn.lms.service.AuthOnboardingService;
import com.example.chemlearn.lms.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor()
public class AuthController {

    private final AuthService authService;
    private final AuthOnboardingService onboardingService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO dto) {
        authService.register(dto);
        return ResponseEntity.ok("Registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponseDTO> googleLogin(
            @Valid @RequestBody GoogleLoginRequestDTO dto) {
        return ResponseEntity.ok(authService.loginWithGoogle(dto.getIdToken()));
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
