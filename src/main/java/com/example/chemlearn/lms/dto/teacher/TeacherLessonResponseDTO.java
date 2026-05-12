package com.example.chemlearn.lms.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherLessonResponseDTO {
    private UUID id;
    private UUID chapterId;
    private String title;
    private String content;
    private Integer durationMinutes;
    private Integer orderIndex;
    private Boolean published;
}
