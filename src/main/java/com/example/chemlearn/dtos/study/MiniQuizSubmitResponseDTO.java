package com.example.chemlearn.dtos.study;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MiniQuizSubmitResponseDTO {
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer score;
    private boolean passed;
}
