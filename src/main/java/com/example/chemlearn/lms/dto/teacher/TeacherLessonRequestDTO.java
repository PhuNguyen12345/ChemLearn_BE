package com.example.chemlearn.lms.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class TeacherLessonRequestDTO {
    @NotNull
    private UUID chapterId;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private Integer estimatedMinutes;

    private Integer orderIndex;

    private Integer displayOrder;

    private Boolean published;

    public Integer getEffectiveOrderIndex() {
        return orderIndex != null ? orderIndex : displayOrder;
    }
}
