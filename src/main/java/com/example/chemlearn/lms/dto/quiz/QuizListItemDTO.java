package com.example.chemlearn.lms.dto.quiz;

import com.example.chemlearn.lms.enums.QuizType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class QuizListItemDTO {
    private UUID id;
    private String title;
    private String description;
    private QuizType quizType;
    private Integer durationMinutes;
    private Integer questionCount;
}


