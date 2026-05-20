package com.example.chemlearn.pvp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * In-memory snapshot of a Pet's combat stats during a battle.
 * NOT a JPA entity — lives only in BattleRoom state.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetState {
    private UUID studentPetId;
    private String petName;
    private String element;     // FIRE, WATER, etc.
    private String imageUrl;
    private String skillName;

    private int currentHp;
    private int maxHp;
    private int attackDamage;   // pre-calculated: baseDamage + level * damageGrowth
    private int petLevel;

    public boolean isAlive() {
        return currentHp > 0;
    }

    public void applyDamage(int damage) {
        this.currentHp = Math.max(0, this.currentHp - damage);
    }
}
