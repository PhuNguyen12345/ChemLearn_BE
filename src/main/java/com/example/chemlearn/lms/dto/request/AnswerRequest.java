package com.example.chemlearn.lms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerRequest {
    private UUID questionId;
    private String content;
    private Boolean isCorrect;
    private Integer orderIndex;
}

