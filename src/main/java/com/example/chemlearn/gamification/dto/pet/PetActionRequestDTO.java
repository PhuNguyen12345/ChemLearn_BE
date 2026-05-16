package com.example.chemlearn.gamification.dto.pet;

import lombok.Data;
import java.util.UUID;

@Data
public class PetActionRequestDTO {
    private UUID itemId;
    private Integer quantity;
}
