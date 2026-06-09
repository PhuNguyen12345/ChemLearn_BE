package com.example.chemlearn.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradedQuestionDTO {
    private Integer questionIndex;
    private Boolean correct;
    private Boolean autoGraded;
    private String studentAnswer;
    private String expectedAnswer;
    private String explanation;
    private String topic;
}
