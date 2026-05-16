package com.example.chemlearn.gamification.controller;

import com.example.chemlearn.gamification.dto.request.ClaimQuestRequest;
import com.example.chemlearn.gamification.dto.response.QuestResponse;
import com.example.chemlearn.gamification.service.QuestService;
import com.example.chemlearn.lms.repository.StudentRepository;
import com.example.chemlearn.core.entity.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/quests")
@RequiredArgsConstructor
public class QuestController {

    private final QuestService questService;
    private final StudentRepository studentRepository;

    @GetMapping("/daily")
    public ResponseEntity<List<QuestResponse>> getDailyQuests() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByUsers_Username(username).orElseThrow(() -> new RuntimeException("Student not found"));
        return ResponseEntity.ok(questService.getDailyQuests(student.getId()));
    }

    @PostMapping("/claim")
    public ResponseEntity<Void> claimQuest(@RequestBody ClaimQuestRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByUsers_Username(username).orElseThrow(() -> new RuntimeException("Student not found"));
        questService.claimQuest(student.getId(), request.getQuestId());
        return ResponseEntity.ok().build();
    }
}
