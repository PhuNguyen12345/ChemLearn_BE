package com.example.chemlearn.dtos.parent;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ParentAssessmentDTO {
    private String type;
    private String title;
    private Integer score;
    private String status;
    private LocalDateTime date;
}
