package com.example.chemlearn.lms.dto.quiz;

import com.example.chemlearn.lms.enums.AttemptStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class StartQuizAttemptResponseDTO {
    private UUID attemptId;
    private UUID quizId;
    private AttemptStatus status;
    private Instant startedAt;
}


