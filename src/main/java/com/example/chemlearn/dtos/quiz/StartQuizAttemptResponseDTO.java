package com.example.chemlearn.dtos.quiz;

import com.example.chemlearn.enums.AttemptStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class StartQuizAttemptResponseDTO {
    private Long attemptId;
    private Long quizId;
    private AttemptStatus status;
    private LocalDateTime startedAt;
}
