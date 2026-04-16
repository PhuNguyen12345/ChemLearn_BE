package com.example.chemlearn.dtos.study;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LessonDetailDTO {
    private Long id;
    private Long chapterId;
    private String chapterTitle;
    private String title;
    private String content;
    private Integer estimatedMinutes;
    private List<MiniQuizQuestionDTO> miniQuizQuestions;
}
