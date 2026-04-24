package com.example.chemlearn.dtos.study;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class MiniQuizSubmitRequestDTO {
    @Valid
    @NotEmpty
    private List<MiniQuizAnswerDTO> answers;
}
