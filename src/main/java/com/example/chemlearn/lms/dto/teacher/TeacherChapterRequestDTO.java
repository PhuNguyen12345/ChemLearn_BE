package com.example.chemlearn.lms.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class TeacherChapterRequestDTO {
    @NotNull
    private UUID classId;

    @NotBlank
    private String title;

    private String description;

    private Integer displayOrder;

    private Boolean published;
}

