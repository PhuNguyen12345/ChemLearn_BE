package com.example.chemlearn.gamification.dto.pet;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PetSpeciesDTO {
    private UUID id;
    private String name;
    private String element;
    private String rarity;
    private String skillName;
    private String skillDescription;
    private String imageUrl;
}
