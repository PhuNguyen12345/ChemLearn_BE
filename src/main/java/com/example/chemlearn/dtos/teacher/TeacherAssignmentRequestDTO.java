package com.example.chemlearn.dtos.teacher;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeacherAssignmentRequestDTO {
    @NotBlank
    private String title;

    @NotNull
    private Long quizId;

    @NotNull
    private Long studentId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dueAt;
}
