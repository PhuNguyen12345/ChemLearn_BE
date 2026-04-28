package com.example.chemlearn.lms.dto.study;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.UUID;

@Data
public class MiniQuizAnswerDTO {
    @NotNull
    private UUID questionId;

    @Pattern(regexp = "^[ABCD]$", message = "selectedOption must be one of A,B,C,D")
    private String selectedOption;
}

