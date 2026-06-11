package com.example.chemlearn.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPackageResponse {
    private String packageCode;
    private Integer gradeLevel;
    private String packageName;
    private String description;
    private Long basePrice;
    private Integer durationDays;
    private String benefitsJson;
    private Boolean isActive;
}
