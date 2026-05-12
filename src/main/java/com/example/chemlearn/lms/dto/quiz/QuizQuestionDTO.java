package com.example.chemlearn.lms.dto.quiz;

import com.example.chemlearn.lms.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class QuizQuestionDTO {
    private UUID id;
    private QuestionType questionType;
    private String prompt;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private Integer displayOrder;
}

