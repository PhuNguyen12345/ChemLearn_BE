package com.example.chemlearn.lms.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class TeacherAssignmentRequestDTO {
    @NotBlank
    private String title;

    @NotNull
    private UUID quizId;

    @NotNull
    private UUID studentId;

    private Instant dueAt;
}

