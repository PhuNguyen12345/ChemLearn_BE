package com.example.chemlearn.lms.dto.teacher;

import com.example.chemlearn.lms.enums.QuestionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TeacherQuizQuestionRequestDTO {
    private QuestionType questionType = QuestionType.SINGLE_CHOICE;
    @NotBlank
    private String prompt;

    private String optionA;

    private String optionB;

    private String optionC;

    private String optionD;

    private String correctOption;

    private String explanation;

    @DecimalMin(value = "0.01", message = "Point value must be greater than 0")
    private BigDecimal pointValue;

    private Integer displayOrder;
}

