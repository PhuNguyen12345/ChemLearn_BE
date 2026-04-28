package com.example.chemlearn.lms.dto.teacher;

import com.example.chemlearn.lms.enums.AttemptStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class TeacherSubmissionDTO {
    private UUID attemptId;
    private String quizTitle;
    private UUID studentId;
    private String studentName;
    private Integer score;
    private AttemptStatus status;
    private Instant submittedAt;
}


