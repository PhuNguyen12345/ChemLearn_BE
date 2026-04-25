package com.example.chemlearn.lms.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class TeacherQuestionBankItemDTO {
    private UUID id;
    private String prompt;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctOption;
    private String explanation;
    private LocalDateTime createdAt;
}

