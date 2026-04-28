package com.example.chemlearn.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudyClassAssignmentResponse {
    private UUID id;
    private UUID classId;
    private String title;
    private UUID labId;
    private UUID quizId;
    private Instant dueDate;
    private Instant createdAt;
}

