package com.example.chemlearn.ai.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmitExamAnswerDTO {
    @NotNull
    @Min(1)
    private Integer questionIndex;

    private String answer;
}
