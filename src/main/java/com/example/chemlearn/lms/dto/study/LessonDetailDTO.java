package com.example.chemlearn.lms.dto.study;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class LessonDetailDTO {
    private UUID id;
    private UUID chapterId;
    private String chapterTitle;
    private String title;
    private String content;
    private Integer estimatedMinutes;
    private String videoUrl;
    private List<MiniQuizQuestionDTO> miniQuizQuestions;
}

