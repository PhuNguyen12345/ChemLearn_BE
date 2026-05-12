package com.example.chemlearn.lms.dto.teacher;

import com.example.chemlearn.lms.enums.QuestionType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeacherQuestionBankRequestDTO {
    private QuestionType questionType = QuestionType.SINGLE_CHOICE;
    @NotBlank
    private String prompt;

    private String optionA;

    private String optionB;

    private String optionC;

    private String optionD;

    private String correctOption;

    private String explanation;
}

