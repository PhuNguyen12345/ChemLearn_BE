package com.example.chemlearn.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttemptAnswerResponse {
    private UUID id;
    private UUID attemptId;
    private UUID questionId;
    private String selectedAnswer;
    private Boolean isCorrect;
}

