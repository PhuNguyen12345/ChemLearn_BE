package com.example.chemlearn.lms.dto.quiz;

import com.example.chemlearn.lms.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
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
    private BigDecimal pointValue;
    private Integer displayOrder;
}

