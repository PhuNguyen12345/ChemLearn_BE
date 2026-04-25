package com.example.chemlearn.lms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudyClassAssignmentRequest {
    private UUID classId;
    private String title;
    private UUID labId;
    private UUID quizId;
    private Instant dueDate;
}

