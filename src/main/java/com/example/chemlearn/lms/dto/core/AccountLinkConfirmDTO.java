package com.example.chemlearn.lms.dto.core;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AccountLinkConfirmDTO {
    @NotBlank
    private String token;
}
