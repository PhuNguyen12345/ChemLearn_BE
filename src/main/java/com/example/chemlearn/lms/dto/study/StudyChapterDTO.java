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
    private Integer gradeLevel;
    private Boolean needPurchase;
    private Boolean hasAccess;
    private String requiredPackageCode;
    private List<LessonSummaryDTO> lessons;
}
