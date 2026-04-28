package com.example.chemlearn.lms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonProgressRequest {
    private UUID studentId;
    private UUID lessonId;
    private Boolean isCompleted;
    private Boolean isLocked;
}

