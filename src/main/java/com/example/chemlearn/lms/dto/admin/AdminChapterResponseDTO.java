package com.example.chemlearn.lms.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminChapterResponseDTO {
    private UUID id;
    private String title;
    private String description;
    private Integer orderIndex;
    private Boolean published;
    private Integer lessonCount;
    private UUID createdBy;
    private Instant createdAt;
    private UUID updatedBy;
    private Instant updatedAt;
    private Integer gradeLevel;
    private Boolean needPurchase;
}
