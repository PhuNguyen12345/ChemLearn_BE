package com.example.chemlearn.lms.dto.request;

import com.example.chemlearn.lms.enums.AttemptStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttemptRequest {
    private UUID studentId;
    private UUID quizId;
    private UUID lessonId;
    private AttemptStatus status;
    private BigDecimal score;
    private Integer totalQuestions;
    private Integer correctAnswers;
}

