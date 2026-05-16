package com.example.chemlearn.gamification.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "pet_species")
public class PetSpecies {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "element", nullable = false, length = 50)
    private String element; // FIRE, WATER, EARTH, AIR, LIGHT, DARK

    @Column(name = "rarity", nullable = false, length = 50)
    private String rarity; // COMMON, RARE, EPIC, LEGENDARY

    @Column(name = "base_hp", nullable = false)
    private Integer baseHp;

    @Column(name = "base_damage", nullable = false)
    private Integer baseDamage;

    @Column(name = "hp_growth", nullable = false)
    private Integer hpGrowth;

    @Column(name = "damage_growth", nullable = false)
    private Integer damageGrowth;

    @Column(name = "skill_name", length = 100)
    private String skillName;

    @Column(name = "skill_description", length = Integer.MAX_VALUE)
    private String skillDescription;

    @Column(name = "image_url", length = Integer.MAX_VALUE)
    private String imageUrl;
}
