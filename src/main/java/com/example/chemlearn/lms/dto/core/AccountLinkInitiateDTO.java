package com.example.chemlearn.lms.dto.core;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AccountLinkInitiateDTO {
    @NotBlank
    @Email
    private String email;
}
