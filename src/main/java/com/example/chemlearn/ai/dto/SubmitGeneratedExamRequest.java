package com.example.chemlearn.ai.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class SubmitGeneratedExamRequest {
    @NotNull
    private UUID studentId;

    @NotNull
    private UUID examId;

    @Valid
    @NotEmpty
    private List<SubmitExamAnswerDTO> answers;
}
