package com.example.chemlearn.gamification.dto.response;

import com.example.chemlearn.gamification.enums.QuestCategory;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class QuestResponse {
    private UUID id;
    private String title;
    private String actionType;
    private Integer targetValue;
    private Integer rewardXp;
    private Integer rewardCoins;
    private Integer currentProgress;
    private Boolean isClaimed;
    private QuestCategory category;
}
