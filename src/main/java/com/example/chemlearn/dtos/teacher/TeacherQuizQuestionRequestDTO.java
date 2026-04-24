package com.example.chemlearn.dtos.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TeacherQuizQuestionRequestDTO {
    @NotBlank
    private String prompt;

    @NotBlank
    private String optionA;

    @NotBlank
    private String optionB;

    @NotBlank
    private String optionC;

    @NotBlank
    private String optionD;

    @Pattern(regexp = "^[ABCD]$", message = "correctOption must be one of A,B,C,D")
    private String correctOption;

    private String explanation;

    private Integer displayOrder;
}
