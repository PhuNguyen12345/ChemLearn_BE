package com.example.chemlearn.gamification.service;

import com.example.chemlearn.gamification.dto.ProgressMapResponse;
import com.example.chemlearn.gamification.dto.MapNodeQuestionResponse;
import java.util.List;
import java.util.UUID;

public interface GamificationMapService {
    ProgressMapResponse getProgressMap(String username);
    List<MapNodeQuestionResponse> getQuestionsForNode(UUID nodeId);
    void completeNode(String username, UUID nodeId, int stars);
}
