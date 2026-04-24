package com.example.chemlearn.dtos.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeacherStudentPerformanceDTO {
    private Long studentId;
    private String studentName;
    private Integer attempts;
    private Integer averageScore;
    private Long completedAssignments;
    private Long pendingAssignments;
}
