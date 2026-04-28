package com.example.chemlearn.lms.dto.parent;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
public class ParentAssessmentDTO {
    private String type;
    private String title;
    private BigDecimal score;
    private String status;
    private Instant date;
}

