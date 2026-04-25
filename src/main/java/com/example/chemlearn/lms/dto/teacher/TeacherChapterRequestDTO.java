package com.example.chemlearn.lms.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeacherChapterRequestDTO {
    @NotBlank
    private String title;

    private String description;

    private Integer displayOrder;

    private Boolean published;
}

