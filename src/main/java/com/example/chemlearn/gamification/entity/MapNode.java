package com.example.chemlearn.gamification.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "map_nodes")
public class MapNode {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "island_id", nullable = false)
    private MapIsland island;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "node_type", nullable = false, length = 50)
    private String nodeType; // QUIZ, LAB, STORY, BOSS

    @Column(name = "target_id")
    private UUID targetId;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "xp_reward", nullable = false)
    private Integer xpReward;

    @Column(name = "monster_name", length = 200)
    private String monsterName;

    @Column(name = "monster_image_url", columnDefinition = "TEXT")
    private String monsterImageUrl;

    @Column(name = "monster_idle_url", columnDefinition = "TEXT")
    private String monsterIdleUrl;

    @Column(name = "monster_attack_url", columnDefinition = "TEXT")
    private String monsterAttackUrl;

    @Column(name = "monster_damaged_url", columnDefinition = "TEXT")
    private String monsterDamagedUrl;
}
