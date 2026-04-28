package com.example.chemlearn.lms.dto.response;

import com.example.chemlearn.lms.enums.QuizType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponse {
    private UUID id;
    private String title;
    private String description;
    private QuizType quizType;
    private Integer durationMinutes;
    private UUID createdByTeacherId;
    private Instant createdAt;
}

