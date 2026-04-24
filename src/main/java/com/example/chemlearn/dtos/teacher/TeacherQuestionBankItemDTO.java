package com.example.chemlearn.dtos.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TeacherQuestionBankItemDTO {
    private Long id;
    private String prompt;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctOption;
    private String explanation;
    private LocalDateTime createdAt;
}
