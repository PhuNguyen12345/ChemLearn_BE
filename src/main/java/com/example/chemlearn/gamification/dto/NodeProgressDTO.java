package com.example.chemlearn.gamification.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class NodeProgressDTO {
    private UUID nodeId;
    private String name;
    private String nodeType;
    private Boolean isCompleted;
    private Boolean isLocked;
    private Integer stars;
    private Integer xpReward;
    private UUID targetId;

    private String monsterName;
    private String monsterImageUrl;
    private String monsterIdleUrl;
    private String monsterAttackUrl;
    private String monsterDamagedUrl;
}
