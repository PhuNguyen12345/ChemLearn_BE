package com.example.chemlearn.lms.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class TeacherStudentPerformanceDTO {
    private UUID studentId;
    private String studentName;
    private Integer attempts;
    private Integer averageScore;
    private Long completedAssignments;
    private Long pendingAssignments;
}

