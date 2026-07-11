package com.example.chemlearn.lms.dto.companion;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class BiCompanionMessageResponseDTO {
    private UUID id;
    private String senderName;
    private String title;
    private String message;
    private String messageType;
    private LocalDate scheduledFor;
    private Instant readAt;
    private Instant createdAt;
}
