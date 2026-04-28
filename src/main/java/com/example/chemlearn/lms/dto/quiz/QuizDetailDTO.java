package com.example.chemlearn.lms.dto.quiz;

import com.example.chemlearn.lms.enums.QuizType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class QuizDetailDTO {
    private UUID id;
    private String title;
    private String description;
    private QuizType quizType;
    private Integer durationMinutes;
    private List<QuizQuestionDTO> questions;
}


