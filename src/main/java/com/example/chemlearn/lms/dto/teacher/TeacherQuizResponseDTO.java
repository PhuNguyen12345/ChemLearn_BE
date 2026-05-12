package com.example.chemlearn.lms.dto.teacher;

import com.example.chemlearn.lms.enums.QuizType;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class TeacherQuizResponseDTO {
    private UUID id;
    private String title;
    private String description;
    private QuizType quizType;
    private Integer durationMinutes;
    private Instant startTime;
    private Instant endTime;
    private Boolean published;
    private Instant createdAt;
    private UUID classId;
}
