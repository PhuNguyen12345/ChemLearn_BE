package com.example.chemlearn.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterResponse {
    private UUID id;
    private String title;
    private String description;
    private Integer gradeLevel;
    private Integer orderIndex;
    private Instant createdAt;
    private Instant updatedAt;
}

