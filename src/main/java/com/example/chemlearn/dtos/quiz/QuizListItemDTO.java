package com.example.chemlearn.dtos.quiz;

import com.example.chemlearn.enums.QuizType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuizListItemDTO {
    private Long id;
    private String title;
    private String description;
    private QuizType quizType;
    private Integer durationMinutes;
    private Integer questionCount;
}
