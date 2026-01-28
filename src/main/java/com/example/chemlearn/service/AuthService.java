package com.example.chemlearn.service;

import com.example.chemlearn.dtos.auth.AuthResponseDTO;
import com.example.chemlearn.dtos.auth.LoginRequestDTO;
import com.example.chemlearn.dtos.auth.RegisterRequestDTO;
import org.springframework.stereotype.Service;

public interface AuthService {
    void register(RegisterRequestDTO dto);

    AuthResponseDTO login(LoginRequestDTO dto);
}
