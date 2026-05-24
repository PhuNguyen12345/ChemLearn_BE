package com.example.chemlearn.gamification.dto.pet;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GachaResultDTO {
    private PetSpeciesDTO species;
    private Boolean isDuplicate;
    private Integer fragmentsReceived;
    private Integer coinsConverted;
}
