package com.example.chemlearn.lms.dto.parent;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ParentChildPerformanceDTO {
    private UUID childId;
    private String childName;
    private Integer attempts;
    private Integer averageScore;
    private Long assignmentsTotal;
    private Long assignmentsCompleted;
}

