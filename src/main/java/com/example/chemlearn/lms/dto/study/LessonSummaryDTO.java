package com.example.chemlearn.lms.dto.study;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class LessonSummaryDTO {
    private UUID id;
    private String title;
    private Integer estimatedMinutes;
}

