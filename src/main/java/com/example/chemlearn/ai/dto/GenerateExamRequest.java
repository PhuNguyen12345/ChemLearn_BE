package com.example.chemlearn.ai.dto;

import com.example.chemlearn.ai.enums.BookType;
import com.example.chemlearn.ai.enums.ExamDifficulty;
import com.example.chemlearn.ai.enums.ExamType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class GenerateExamRequest {
    @NotNull
    private UUID studentId;

    @NotNull
    @Min(6)
    @Max(9)
    private Integer grade;

    @NotNull
    private BookType bookType;

    @NotNull
    private ExamType examType;

    @NotBlank
    private String topic;

    @NotNull
    private ExamDifficulty difficulty;
}
