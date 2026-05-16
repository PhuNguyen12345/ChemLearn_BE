package com.example.chemlearn.lms.dto.parent;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParentDashboardOverviewDTO {
    private Double recentAverageScore;
    private Long completedAssignments;
    private Long totalAssignments;
    private Double completionPercentage;
}
