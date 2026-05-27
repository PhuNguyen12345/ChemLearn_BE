package com.example.chemlearn.gamification.controller;

import com.example.chemlearn.gamification.dto.ProgressMapResponse;
import com.example.chemlearn.gamification.dto.MapNodeQuestionResponse;
import com.example.chemlearn.gamification.dto.CompleteNodeRequest;
import com.example.chemlearn.gamification.service.GamificationMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @GetMapping("/nodes/{nodeId}/questions")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<MapNodeQuestionResponse>> getQuestionsForNode(@PathVariable UUID nodeId) {
        return ResponseEntity.ok(gamificationMapService.getQuestionsForNode(nodeId));
    }

    @PostMapping("/nodes/{nodeId}/complete")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> completeNode(
            @PathVariable UUID nodeId,
            @RequestBody CompleteNodeRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        gamificationMapService.completeNode(username, nodeId, request.getStars());
        return ResponseEntity.ok().build();
    }
}
