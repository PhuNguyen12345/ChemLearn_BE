package com.example.chemlearn.ai.dto;

import com.example.chemlearn.ai.enums.MasteryLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyzeResultResponse {
    private MasteryLevel masteryLevel;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<RecommendedLessonDTO> recommendedLessons;
    private List<SuggestedLabDTO> recommendedLabs;
}
