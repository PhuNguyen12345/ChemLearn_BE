package com.example.chemlearn.gamification.dto.pet;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class StudentPetDTO {
    private UUID id;
    private PetSpeciesDTO species;
    private Integer level;
    private Integer experience;
    private Integer starLevel;
    
    // Calculated stats
    private Integer maxHp;
    private Integer damage;
    private Integer nextLevelExp;
    private Integer fragments;
}
