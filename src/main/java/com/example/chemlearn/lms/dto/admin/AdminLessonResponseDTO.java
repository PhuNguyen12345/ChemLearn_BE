package com.example.chemlearn.lms.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminLessonResponseDTO {
    private UUID id;
    private UUID chapterId;
    private String chapterTitle;
    private String title;
    private String content;
    private Integer durationMinutes;
    private Integer orderIndex;
    private Boolean published;
    private String videoUrl;
    private List<AdminMiniQuizQuestionDTO> miniQuizQuestions;
    private UUID createdBy;
    private Instant createdAt;
    private UUID updatedBy;
    private Instant updatedAt;
}
