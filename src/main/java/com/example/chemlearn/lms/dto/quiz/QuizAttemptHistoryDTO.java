package com.example.chemlearn.lms.dto.quiz;

import com.example.chemlearn.lms.enums.AttemptStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class QuizAttemptHistoryDTO {
    @NotNull
    private UUID quizAttemptId;

    private AttemptStatus status;

    private BigDecimal score;

    private Integer totalQuestion;

    private Integer correctAnswer;

    private Instant startedAt;

    private Instant submittedAt;
    private boolean canRetake;
}
