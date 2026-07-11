package com.example.chemlearn.ai.dto;

import com.example.chemlearn.ai.enums.BookType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AiChatRequest {
    @NotNull
    private UUID studentId;

    private UUID sessionId;

    @NotNull
    @Min(6)
    @Max(9)
    private Integer grade;

    @NotNull
    private BookType bookType;

    private String topic;

    @NotBlank
    private String message;
}
