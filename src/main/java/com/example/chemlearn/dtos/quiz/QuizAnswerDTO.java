package com.example.chemlearn.dtos.quiz;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class QuizAnswerDTO {
    @NotNull
    private Long questionId;

    @Pattern(regexp = "^[ABCD]$", message = "selectedOption must be one of A,B,C,D")
    private String selectedOption;
}
