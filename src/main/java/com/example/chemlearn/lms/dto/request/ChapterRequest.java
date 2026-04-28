package com.example.chemlearn.lms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterRequest {
    private String title;
    private String description;
    private Integer gradeLevel;
    private Integer orderIndex;
}

