package com.example.chemlearn.lab.dto.response;

import com.example.chemlearn.lab.enums.LabType;

import java.time.LocalDateTime;
import java.util.UUID;

public class LabSummaryResponse {
    private UUID id;
    private String title;
    private String type;
    private String description;
    private String category;
    private String difficulty;
    private LocalDateTime lastEdited;
}
