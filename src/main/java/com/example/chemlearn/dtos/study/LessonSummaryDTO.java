package com.example.chemlearn.dtos.study;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LessonSummaryDTO {
    private Long id;
    private String title;
    private Integer estimatedMinutes;
}
