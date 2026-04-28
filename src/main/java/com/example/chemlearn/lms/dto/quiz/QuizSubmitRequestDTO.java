package com.example.chemlearn.lms.dto.quiz;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class QuizSubmitRequestDTO {
    @Valid
    @NotEmpty
    private List<QuizAnswerDTO> answers;
}

