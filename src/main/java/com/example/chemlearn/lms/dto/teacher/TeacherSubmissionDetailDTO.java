package com.example.chemlearn.lms.dto.teacher;

import com.example.chemlearn.lms.enums.AttemptStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherSubmissionDetailDTO {
    private UUID attemptId;
    private String quizTitle;
    private String studentName;
    private Integer score;
    private AttemptStatus status;
    private Instant submittedAt;
    private List<TeacherAttemptAnswerDTO> answers;
}
