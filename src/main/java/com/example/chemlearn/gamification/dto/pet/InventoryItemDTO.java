package com.example.chemlearn.gamification.dto.pet;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class InventoryItemDTO {
    private UUID id;
    private UUID itemId;
    private String name;
    private String itemType;
    private String description;
    private Integer quantity;
    private String imageUrl;
}
