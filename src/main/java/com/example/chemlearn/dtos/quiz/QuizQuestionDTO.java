package com.example.chemlearn.dtos.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuizQuestionDTO {
    private Long id;
    private String prompt;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private Integer displayOrder;
}
