package com.example.chemlearn.lms.dto.request;

import com.example.chemlearn.lms.enums.QuizType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizRequest {
    private String title;
    private String description;
    private QuizType quizType;
    private Integer durationMinutes;
    private UUID createdByTeacherId;
}

