package com.example.chemlearn.lms.dto.feedback;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackReportStatusUpdateDTO {
    @NotBlank
    @Size(max = 30)
    private String status;

    private String adminNote;
}
