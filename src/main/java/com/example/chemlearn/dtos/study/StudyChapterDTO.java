package com.example.chemlearn.dtos.study;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class StudyChapterDTO {
    private Long id;
    private String title;
    private String description;
    private List<LessonSummaryDTO> lessons;
}
