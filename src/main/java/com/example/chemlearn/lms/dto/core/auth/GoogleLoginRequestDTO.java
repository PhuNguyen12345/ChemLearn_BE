package com.example.chemlearn.lms.dto.core.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginRequestDTO {
    @NotBlank(message = "ID token must not be blank")
    private String idToken;
}
