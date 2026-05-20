package com.example.chemlearn.lms.dto.core.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AccessRequestCreateDTO {
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Role must not be blank")
    private String role; // ROLE_TEACHER or ROLE_PARENT

    @NotBlank(message = "Full name must not be blank")
    private String fullName;

    private String additionalInfo;
}
