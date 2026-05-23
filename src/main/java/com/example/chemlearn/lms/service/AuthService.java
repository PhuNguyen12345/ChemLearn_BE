package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.dto.core.auth.AuthResponseDTO;
import com.example.chemlearn.lms.dto.core.auth.GoogleLoginRequestDTO;
import com.example.chemlearn.lms.dto.core.auth.LoginRequestDTO;
import com.example.chemlearn.lms.dto.core.auth.OtpVerifyRequestDTO;
import com.example.chemlearn.lms.dto.core.auth.RegisterRequestDTO;

public interface AuthService {
    void register(RegisterRequestDTO dto);

    void registerWithOtp(RegisterRequestDTO dto);

    void registerTeacherParentPending(RegisterRequestDTO dto);

    void verifyOtpAndCreateAccount(OtpVerifyRequestDTO dto);

    void resendOtp(String email);

    AuthResponseDTO login(LoginRequestDTO dto);

    AuthResponseDTO loginWithGoogle(GoogleLoginRequestDTO dto);

    void logout(String token);
}
