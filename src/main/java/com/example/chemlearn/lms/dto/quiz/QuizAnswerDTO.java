package com.example.chemlearn.lms.dto.quiz;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class QuizAnswerDTO {
    @NotNull
    private UUID questionId;

    private String selectedOption;
}

