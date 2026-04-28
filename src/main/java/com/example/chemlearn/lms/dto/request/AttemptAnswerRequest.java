package com.example.chemlearn.lms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttemptAnswerRequest {
    private UUID attemptId;
    private UUID questionId;
    private UUID selectedAnswerId;
}

