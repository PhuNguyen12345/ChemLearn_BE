package com.example.chemlearn.gamification.service;

import com.example.chemlearn.gamification.dto.response.QuestResponse;

import java.util.List;
import java.util.UUID;

public interface QuestService {
    List<QuestResponse> getDailyQuests(UUID studentId);
    void updateProgress(UUID studentId, String actionType, int amount);
    QuestResponse claimQuest(UUID studentId, UUID questId);
}
