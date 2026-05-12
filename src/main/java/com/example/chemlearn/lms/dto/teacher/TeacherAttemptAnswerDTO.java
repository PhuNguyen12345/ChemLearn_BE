package com.example.chemlearn.lms.dto.teacher;

import com.example.chemlearn.lms.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherAttemptAnswerDTO {
    private UUID questionId;
    private String prompt;
    private QuestionType questionType;
    private String selectedOption;
    private String correctOption;
    private Boolean isCorrect;
}
