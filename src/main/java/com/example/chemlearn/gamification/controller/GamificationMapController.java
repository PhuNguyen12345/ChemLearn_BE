package com.example.chemlearn.gamification.controller;

import com.example.chemlearn.gamification.dto.ProgressMapResponse;
import com.example.chemlearn.gamification.service.GamificationMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/gamification/map")
@RequiredArgsConstructor
public class GamificationMapController {

    private final GamificationMapService gamificationMapService;

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ProgressMapResponse> getProgressMap(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(gamificationMapService.getProgressMap(username));
    }
}
