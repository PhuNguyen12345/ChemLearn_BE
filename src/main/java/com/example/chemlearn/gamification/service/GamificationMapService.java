package com.example.chemlearn.gamification.service;

import com.example.chemlearn.gamification.dto.ProgressMapResponse;

public interface GamificationMapService {
    ProgressMapResponse getProgressMap(String username);
}
