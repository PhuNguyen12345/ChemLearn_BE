package com.example.chemlearn.gamification.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AdminEggDropRateDTO {
    private UUID id;

    @NotNull
    private UUID eggItemId;

    private String eggItemName;

    @NotNull
    private UUID petSpeciesId;

    private String petSpeciesName;

    @NotNull
    @Min(1)
    private Integer dropWeight;
}
