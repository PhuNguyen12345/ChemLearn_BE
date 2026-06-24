package com.example.chemlearn.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiTtsRequest {
    @NotBlank(message = "Text is required")
    @Size(max = 50000, message = "Text is too long")
    private String text;
}
