package com.example.chemlearn.gamification.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class MapNodeQuestionResponse {
    private UUID id;
    private String prompt;
    private List<String> options;
    private String correctAnswer;
    private String explanation;
}
