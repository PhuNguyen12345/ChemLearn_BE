package com.example.chemlearn.lms.dto.core.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InviteAcceptRequestDTO {
    @NotBlank(message = "Token must not be blank")
    private String token;

    @NotBlank(message = "Username must not be blank")
    private String username;

    @NotBlank(message = "Password must not be blank")
    private String password;

    private String fullName;

    // Teacher specific fields
    private String bio;
    private String specialization;
    private String degree;
    private String workplace;

    // Parent specific fields
    private String phoneNumber;
    private String jobTitle;
}
