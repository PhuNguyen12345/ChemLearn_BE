package com.example.chemlearn.gamification.entity;

import com.example.chemlearn.gamification.enums.QuestCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "quests")
public class Quest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Column(name = "target_value", nullable = false)
    private Integer targetValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private QuestCategory category;

    @Column(name = "reward_xp", nullable = false)
    private Integer rewardXp;

    @ColumnDefault("0")
    @Column(name = "reward_coins")
    private Integer rewardCoins = 0;

    @ColumnDefault("true")
    @Column(name = "is_active")
    private Boolean isActive;

}