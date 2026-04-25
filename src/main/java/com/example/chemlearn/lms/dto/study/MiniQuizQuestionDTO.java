package com.example.chemlearn.lms.dto.study;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class MiniQuizQuestionDTO {
    private UUID id;
    private String prompt;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
}

