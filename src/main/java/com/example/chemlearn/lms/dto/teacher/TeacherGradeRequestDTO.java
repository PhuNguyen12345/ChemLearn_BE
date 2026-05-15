package com.example.chemlearn.lms.dto.teacher;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class TeacherGradeRequestDTO {
    private Integer finalScore;
    private String feedback;
    private List<EssayGradeDTO> essayGrades;

    @Data
    public static class EssayGradeDTO {
        private UUID questionId;
        private Boolean isCorrect;
        private BigDecimal awardedPoints;
    }
}
