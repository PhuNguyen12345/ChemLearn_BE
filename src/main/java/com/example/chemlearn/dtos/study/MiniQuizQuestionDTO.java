package com.example.chemlearn.dtos.study;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MiniQuizQuestionDTO {
    private Long id;
    private String prompt;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
}
