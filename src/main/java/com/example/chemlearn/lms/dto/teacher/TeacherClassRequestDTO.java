package com.example.chemlearn.lms.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeacherClassRequestDTO {
    @NotBlank
    private String name;

    private String schedule;

    private String description;

    private Integer gradeLevel;
}