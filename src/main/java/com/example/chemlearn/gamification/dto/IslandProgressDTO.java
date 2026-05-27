package com.example.chemlearn.gamification.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class IslandProgressDTO {
    private UUID islandId;
    private String name;
    private String description;
    private Boolean isLocked;
    private Integer requiredLevel;
    private String imageUrl;
    private List<NodeProgressDTO> nodes;
}
