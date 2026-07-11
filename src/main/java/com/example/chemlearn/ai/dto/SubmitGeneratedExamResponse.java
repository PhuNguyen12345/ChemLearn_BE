package com.example.chemlearn.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitGeneratedExamResponse {
    private Integer correct;
    private Integer total;
    private Integer scorePercent;
    private List<GradedQuestionDTO> gradedQuestions;
    private AnalyzeResultResponse analysis;
}
