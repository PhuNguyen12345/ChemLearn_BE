package com.example.chemlearn.lms.dto.parent;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ScoreTimelineDTO {
    private String quizTitle;
    private Double score;
    private Instant submittedAt;
}
