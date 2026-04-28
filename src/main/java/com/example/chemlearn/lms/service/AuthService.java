package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.dto.core.auth.AuthResponseDTO;
import com.example.chemlearn.lms.dto.core.auth.LoginRequestDTO;
import com.example.chemlearn.lms.dto.core.auth.RegisterRequestDTO;

public interface AuthService {
    void register(RegisterRequestDTO dto);

    AuthResponseDTO login(LoginRequestDTO dto);

    void logout(String token);
}

