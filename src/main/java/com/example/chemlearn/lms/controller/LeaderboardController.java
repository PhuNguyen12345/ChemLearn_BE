package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.leaderboard.LeaderboardResponseDTO;
import com.example.chemlearn.lms.enums.LeaderboardCategory;
import com.example.chemlearn.lms.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<LeaderboardResponseDTO> getLeaderboard(
            Authentication authentication,
            @RequestParam(defaultValue = "EXPERIENCE") LeaderboardCategory category,
            @RequestParam(defaultValue = "50") int limit
    ) {
        String username = authentication.getName();
        LeaderboardResponseDTO response = leaderboardService.getLeaderboard(username, category, limit);
        return ResponseEntity.ok(response);
    }
}
