package com.example.chemlearn.lms.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeacherDashboardSummaryDTO {
    private Integer totalStudents;
    private Integer averageScore;
    private Long totalLessons;
    private Long totalQuizzes;
    private Long pendingAssignments;
}

