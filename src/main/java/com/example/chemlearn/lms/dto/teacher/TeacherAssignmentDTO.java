package com.example.chemlearn.lms.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class TeacherAssignmentDTO {
    private UUID id;
    private String title;
    private UUID quizId;
    private UUID classId;
    private Instant dueDate;
    private String status;
}
