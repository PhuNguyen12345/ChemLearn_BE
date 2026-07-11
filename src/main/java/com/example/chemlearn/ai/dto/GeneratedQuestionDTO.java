package com.example.chemlearn.ai.dto;

import com.example.chemlearn.ai.enums.AiQuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedQuestionDTO {
    private AiQuestionType type;
    private String question;
    private List<String> options;
    private String answer;
    private String explanation;
    private String topic;
}
