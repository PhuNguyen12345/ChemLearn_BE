package com.example.chemlearn.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinClassRequestDTO {
    @NotBlank
    private String classCode;
}