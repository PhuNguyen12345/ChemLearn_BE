package com.example.chemlearn.lms.dto.companion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AdminBiCompanionMessageRequestDTO {
    @NotNull
    private UUID studentId;

    @NotBlank
    @Size(max = 180)
    private String title;

    @NotBlank
    private String message;
}
