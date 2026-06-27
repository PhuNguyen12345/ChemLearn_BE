package com.example.chemlearn.lms.dto.feedback;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackReportRequestDTO {
    @NotBlank
    @Size(max = 30)
    private String type;

    @NotBlank
    @Size(max = 30)
    private String priority;

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    private String message;
}
