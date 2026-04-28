package com.example.chemlearn.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudyClassResponse {
    private UUID id;
    private String name;
    private Integer gradeLevel;
    private UUID teacherId;
    private Instant createdAt;
    private Instant updatedAt;
}

