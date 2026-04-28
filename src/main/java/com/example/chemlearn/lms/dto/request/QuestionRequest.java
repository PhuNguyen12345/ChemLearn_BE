package com.example.chemlearn.lms.dto.request;

import com.example.chemlearn.lms.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest {
    private UUID lessonId;
    private UUID quizId;
    private String content;
    private QuestionType questionType;
    private String explanation;
    private Integer orderIndex;
}

