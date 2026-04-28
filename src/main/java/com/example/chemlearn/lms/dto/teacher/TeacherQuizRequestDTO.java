package com.example.chemlearn.lms.dto.teacher;

import com.example.chemlearn.lms.enums.QuizType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TeacherQuizRequestDTO {
    @NotBlank
    private String title;

    private String description;

    @NotNull
    private QuizType quizType;

    private Integer durationMinutes;

    private Boolean published;
}


