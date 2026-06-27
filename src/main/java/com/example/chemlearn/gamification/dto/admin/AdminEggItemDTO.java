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
public class AdminEggItemDTO {
    private UUID id;

    @NotBlank
    @Size(max = 200)
    private String name;

    private String description;

    @NotNull
    @Min(0)
    private Integer priceCoins;

    private Integer effectValue;
    private String imageUrl;
}
