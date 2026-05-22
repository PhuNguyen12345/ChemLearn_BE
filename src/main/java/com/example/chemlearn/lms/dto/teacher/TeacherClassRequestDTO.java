package com.example.chemlearn.lms.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeacherClassRequestDTO {
    @NotBlank
    private String name;

    private Integer gradeLevel;

    private String description;

    private Integer grade;

    private String classType;
}