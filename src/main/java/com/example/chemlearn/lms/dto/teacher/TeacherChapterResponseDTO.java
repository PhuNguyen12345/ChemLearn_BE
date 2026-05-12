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
public class TeacherChapterResponseDTO {
    private UUID id;
    private String title;
    private String description;
    private Integer orderIndex;
    private Boolean published;
}
