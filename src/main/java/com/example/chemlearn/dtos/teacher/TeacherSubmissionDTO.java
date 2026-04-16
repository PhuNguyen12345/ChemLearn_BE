package com.example.chemlearn.dtos.teacher;

import com.example.chemlearn.enums.AttemptStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TeacherSubmissionDTO {
    private Long attemptId;
    private String quizTitle;
    private Long studentId;
    private String studentName;
    private Integer score;
    private AttemptStatus status;
    private LocalDateTime submittedAt;
}
