package com.example.chemlearn.gamification.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GamificationProfileResponse {
    private Integer experience;
    private Integer level;
    private Integer currentStreak;
    private Integer coins;
}
