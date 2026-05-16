package com.example.chemlearn.gamification.controller;

import com.example.chemlearn.gamification.dto.response.GamificationProfileResponse;
import com.example.chemlearn.gamification.service.GamificationProfileService;
import com.example.chemlearn.lms.repository.StudentRepository;
import com.example.chemlearn.core.entity.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/gamification/profile")
@RequiredArgsConstructor
public class GamificationProfileController {

    private final GamificationProfileService gamificationProfileService;
    private final StudentRepository studentRepository;

    @GetMapping
    public ResponseEntity<GamificationProfileResponse> getProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByUsers_Username(username).orElseThrow(() -> new RuntimeException("Student not found"));
        return ResponseEntity.ok(gamificationProfileService.getProfile(student.getId()));
    }

    @PostMapping("/activity")
    public ResponseEntity<Void> logDailyActivity() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByUsers_Username(username).orElseThrow(() -> new RuntimeException("Student not found"));
        gamificationProfileService.updateStreak(student.getId());
        return ResponseEntity.ok().build();
    }
}
