package com.example.chemlearn.lms.dto.core.auth;

import lombok.Data;
import java.util.UUID;

@Data
public class AuthResponseDTO {
    private String token;
    private String id;
    private String username;
    private String email;
    private String role;
    private String fullName;
    private String avatarUrl;
    private Boolean isActive;
}

