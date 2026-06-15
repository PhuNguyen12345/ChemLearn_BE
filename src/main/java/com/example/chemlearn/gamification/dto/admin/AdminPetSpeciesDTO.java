package com.example.chemlearn.gamification.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AdminPetSpeciesDTO {
    private UUID id;

    @NotBlank
    @Size(max = 200)
    private String name;

    @NotBlank
    @Size(max = 50)
    private String element;

    @NotBlank
    @Size(max = 50)
    private String rarity;

    @NotNull
    @Min(1)
    private Integer baseHp;

    @NotNull
    @Min(1)
    private Integer baseDamage;

    @NotNull
    @Min(0)
    private Integer hpGrowth;

    @NotNull
    @Min(0)
    private Integer damageGrowth;

    private String skillName;
    private String skillDescription;
    private String imageUrl;
}
