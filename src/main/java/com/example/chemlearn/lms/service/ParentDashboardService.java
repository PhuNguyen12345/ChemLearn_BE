package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.dto.parent.ChildGamificationDTO;
import com.example.chemlearn.lms.dto.parent.ChildProfileDTO;
import com.example.chemlearn.lms.dto.parent.ParentDashboardOverviewDTO;
import com.example.chemlearn.lms.dto.parent.ScoreTimelineDTO;

import java.util.List;
import java.util.UUID;

public interface ParentDashboardService {
    List<ChildProfileDTO> getChildrenByParent(String username);
    ParentDashboardOverviewDTO getChildOverview(String username, UUID studentId);
    ChildGamificationDTO getChildGamification(String username, UUID studentId);
    List<ScoreTimelineDTO> getChildScoreTimeline(String username, UUID studentId);
}
