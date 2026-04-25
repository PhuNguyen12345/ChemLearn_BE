package com.example.chemlearn.lms.dto.core.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDTO {
    @NotBlank
    @Size(min = 4, max = 30)
    private String username;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_.]).{8,32}$",
            message = "Password must be 8-32 chars, include upper, lower, number and special char"
    )
    private String password;
}

