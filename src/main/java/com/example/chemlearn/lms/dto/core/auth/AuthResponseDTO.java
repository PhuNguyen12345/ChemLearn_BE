package com.example.chemlearn.lms.dto.core.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private String id;
    private String username;
    private String email;
    private String role;
}

