package com.example.chemlearn.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResponse {
    private UUID id;
    private UUID questionId;
    private String content;
    private Boolean isCorrect;
    private Integer orderIndex;
}

