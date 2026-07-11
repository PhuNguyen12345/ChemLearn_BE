package com.example.chemlearn.lms.dto.feedback;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class FeedbackReportResponseDTO {
    private UUID id;
    private UUID reporterId;
    private String reporterName;
    private String reporterEmail;
    private String type;
    private String priority;
    private String title;
    private String message;
    private String status;
    private String adminNote;
    private Instant createdAt;
    private Instant updatedAt;
}
