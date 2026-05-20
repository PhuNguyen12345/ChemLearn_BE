package com.example.chemlearn.gamification.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProgressMapResponse {
    private List<IslandProgressDTO> islands;
    private Integer totalStars;
    private Integer currentLevel;
}
