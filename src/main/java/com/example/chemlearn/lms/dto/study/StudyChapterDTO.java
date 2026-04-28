package com.example.chemlearn.lms.dto.study;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class StudyChapterDTO {
    private UUID id;
    private String title;
    private String description;
    private List<LessonSummaryDTO> lessons;
}

