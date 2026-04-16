package com.example.chemlearn.dtos.quiz;

import com.example.chemlearn.enums.AttemptStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class QuizSubmitResponseDTO {
    private Long attemptId;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer score;
    private AttemptStatus status;
    private LocalDateTime submittedAt;
}
