package com.example.chemlearn.gamification.service;

import com.example.chemlearn.gamification.dto.response.GamificationProfileResponse;
import com.example.chemlearn.gamification.enums.XpSource;

import java.util.UUID;

public interface GamificationProfileService {
    GamificationProfileResponse getProfile(UUID studentId);
    void updateStreak(UUID studentId);
    void addExpAndCoins(UUID studentId, int expAmount, int coinsAmount, XpSource source, String description);
}
