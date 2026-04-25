package com.example.chemlearn.lms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonRequest {
    private UUID chapterId;
    private UUID labId;
    private String title;
    private String contentType;
    private String videoUrl;
    private String textContent;
    private Integer durationMinutes;
    private Integer orderIndex;
}

