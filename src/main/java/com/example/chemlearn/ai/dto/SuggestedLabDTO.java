package com.example.chemlearn.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestedLabDTO {
    private UUID id;
    private String title;
    private String description;
    private String category;
    private String difficulty;
}
