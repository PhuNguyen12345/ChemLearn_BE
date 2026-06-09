package com.example.chemlearn.ai.dto;

import com.example.chemlearn.ai.enums.BookType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AnalyzeResultRequest {
    @NotNull
    private UUID studentId;

    @NotNull
    @Min(6)
    @Max(9)
    private Integer grade;

    @NotNull
    private BookType bookType;

    @NotBlank
    private String topic;

    @NotNull
    @Min(0)
    private Integer correct;

    @NotNull
    @Min(1)
    private Integer total;

    private List<String> wrongTopics;
}
