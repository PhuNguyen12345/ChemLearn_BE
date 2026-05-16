package com.example.chemlearn.gamification.entity;

import com.example.chemlearn.gamification.enums.ItemType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 50)
    private ItemType itemType;

    @Column(name = "price_coins", nullable = false)
    private Integer priceCoins;

    @Column(name = "effect_value")
    private Integer effectValue; // For FOOD: EXP amount.

    @Column(name = "image_url", length = Integer.MAX_VALUE)
    private String imageUrl;
}
