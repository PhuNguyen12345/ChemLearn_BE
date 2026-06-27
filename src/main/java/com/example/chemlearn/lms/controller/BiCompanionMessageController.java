package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.companion.BiCompanionMessageResponseDTO;
import com.example.chemlearn.lms.service.BiCompanionMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student/bi/messages")
@PreAuthorize("hasRole('STUDENT')")
public class BiCompanionMessageController {
    private final BiCompanionMessageService biCompanionMessageService;

    @GetMapping
    public ResponseEntity<List<BiCompanionMessageResponseDTO>> findMyMessages(Authentication authentication) {
        return ResponseEntity.ok(biCompanionMessageService.findMessagesForStudent(authentication.getName()));
    }
}
