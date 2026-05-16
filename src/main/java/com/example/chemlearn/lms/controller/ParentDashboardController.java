package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.parent.ChildGamificationDTO;
import com.example.chemlearn.lms.dto.parent.ChildProfileDTO;
import com.example.chemlearn.lms.dto.parent.ParentDashboardOverviewDTO;
import com.example.chemlearn.lms.dto.parent.ScoreTimelineDTO;
import com.example.chemlearn.lms.service.ParentDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/parent/children")
@RequiredArgsConstructor
public class ParentDashboardController {

    private final ParentDashboardService parentDashboardService;

    @GetMapping
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<ChildProfileDTO>> getChildren(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(parentDashboardService.getChildrenByParent(username));
    }

    @GetMapping("/{studentId}/overview")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ParentDashboardOverviewDTO> getChildOverview(
            @PathVariable UUID studentId,
            Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(parentDashboardService.getChildOverview(username, studentId));
    }

    @GetMapping("/{studentId}/gamification")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ChildGamificationDTO> getChildGamification(
            @PathVariable UUID studentId,
            Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(parentDashboardService.getChildGamification(username, studentId));
    }

    @GetMapping("/{studentId}/timeline")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<ScoreTimelineDTO>> getChildScoreTimeline(
            @PathVariable UUID studentId,
            Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(parentDashboardService.getChildScoreTimeline(username, studentId));
    }
}
