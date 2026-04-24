package com.example.chemlearn.dtos.quiz;

import com.example.chemlearn.enums.QuizType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QuizDetailDTO {
    private Long id;
    private String title;
    private String description;
    private QuizType quizType;
    private Integer durationMinutes;
    private List<QuizQuestionDTO> questions;
}
