package com.example.chemlearn.dtos.parent;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParentChildPerformanceDTO {
    private Long childId;
    private String childName;
    private Integer attempts;
    private Integer averageScore;
    private Long assignmentsTotal;
    private Long assignmentsCompleted;
}
