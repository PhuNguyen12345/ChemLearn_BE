package com.example.chemlearn.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonProgressResponse {
    private UUID id;
    private UUID studentId;
    private UUID lessonId;
    private Boolean isCompleted;
    private Boolean isLocked;
    private Instant lastAccessedAt;
}

