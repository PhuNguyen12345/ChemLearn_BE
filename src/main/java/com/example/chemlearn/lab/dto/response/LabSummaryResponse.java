package com.example.chemlearn.lab.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LabSummaryResponse {
    private UUID id;
    private String title;
    private String type;
    private String description;
    private String category;
    private String difficulty;
    private Instant updatedAt;
}
