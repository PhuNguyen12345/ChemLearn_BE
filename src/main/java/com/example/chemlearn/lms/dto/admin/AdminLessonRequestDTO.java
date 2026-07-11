package com.example.chemlearn.lms.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminLessonRequestDTO {
    @NotNull(message = "Chapter ID is required")
    private UUID chapterId;

    @NotBlank(message = "Lesson title is required")
    private String title;

    @NotBlank(message = "Lesson content is required")
    private String content;

    private Integer durationMinutes = 15;

    private Integer orderIndex;

    private Boolean published = true;

    private String videoUrl;

    private List<AdminMiniQuizQuestionDTO> miniQuizQuestions;
}
