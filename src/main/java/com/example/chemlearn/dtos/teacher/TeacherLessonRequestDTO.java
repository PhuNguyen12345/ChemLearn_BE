package com.example.chemlearn.dtos.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TeacherLessonRequestDTO {
    @NotNull
    private Long chapterId;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private Integer estimatedMinutes;

    private Integer displayOrder;

    private Boolean published;
}
