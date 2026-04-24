package com.example.chemlearn.dtos.teacher;

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
