package com.example.chemlearn.lms.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterAverageScoreDTO {
    private String chapterName;
    private Double averageScore;
}
