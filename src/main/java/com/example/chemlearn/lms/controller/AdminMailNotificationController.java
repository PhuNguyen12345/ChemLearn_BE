package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.admin.MailAnnouncementRequestDTO;
import com.example.chemlearn.lms.service.AutoMailNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/mail-notifications")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminMailNotificationController {

    private final AutoMailNotificationService autoMailNotificationService;

    @PostMapping("/broadcast")
    public ResponseEntity<Map<String, Object>> broadcast(@Valid @RequestBody MailAnnouncementRequestDTO request) {
        int queuedEmails = autoMailNotificationService.sendAdminAnnouncement(request);
        return ResponseEntity.ok(Map.of(
                "message", "Mail notification queued",
                "queuedEmails", queuedEmails
        ));
    }
}
