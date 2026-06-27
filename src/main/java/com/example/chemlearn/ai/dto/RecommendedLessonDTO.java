package com.example.chemlearn.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedLessonDTO {
    private UUID id;
    private String title;
    private String chapterTitle;
    private Integer durationMinutes;
    private String topic;
}
