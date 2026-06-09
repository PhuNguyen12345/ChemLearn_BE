package com.example.chemlearn.ai.dto;

import com.example.chemlearn.ai.enums.BookType;
import com.example.chemlearn.ai.enums.ExamDifficulty;
import com.example.chemlearn.ai.enums.ExamType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedExamSummaryResponse {
    private UUID id;
    private String title;
    private Integer grade;
    private BookType bookType;
    private ExamType examType;
    private String topic;
    private ExamDifficulty difficulty;
    private Integer durationMinutes;
    private Integer questionCount;
    private Instant createdAt;
}
