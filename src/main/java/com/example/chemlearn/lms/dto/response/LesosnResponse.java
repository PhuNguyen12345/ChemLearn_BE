package com.example.chemlearn.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LesosnResponse {
    private UUID id;
    private UUID chapterId;
    private UUID labId;
    private String title;
    private String contentType;
    private String videoUrl;
    private String textContent;
    private Integer durationMinutes;
    private Integer orderIndex;
    private Instant createdAt;
    private Instant updatedAt;
}

