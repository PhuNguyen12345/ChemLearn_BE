package com.example.chemlearn.lms.dto.quiz;

import com.example.chemlearn.lms.enums.AttemptStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class QuizSubmitResponseDTO {
    private UUID attemptId;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer score;
    private AttemptStatus status;
    private Instant submittedAt;
}


