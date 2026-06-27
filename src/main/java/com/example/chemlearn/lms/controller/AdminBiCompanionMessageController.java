package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.companion.AdminBiCompanionMessageRequestDTO;
import com.example.chemlearn.lms.dto.companion.BiCompanionMessageResponseDTO;
import com.example.chemlearn.lms.service.BiCompanionMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/bi/messages")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBiCompanionMessageController {
    private final BiCompanionMessageService biCompanionMessageService;

    @PostMapping
    public ResponseEntity<BiCompanionMessageResponseDTO> sendMessage(
            @Valid @RequestBody AdminBiCompanionMessageRequestDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(biCompanionMessageService.sendAdminMessageToStudent(
                        request.getStudentId(),
                        request.getTitle(),
                        request.getMessage()
                ));
    }
}
