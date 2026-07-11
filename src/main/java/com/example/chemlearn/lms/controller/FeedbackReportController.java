package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.feedback.FeedbackReportRequestDTO;
import com.example.chemlearn.lms.dto.feedback.FeedbackReportResponseDTO;
import com.example.chemlearn.lms.dto.feedback.FeedbackReportStatusUpdateDTO;
import com.example.chemlearn.lms.service.FeedbackReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FeedbackReportController {
    private final FeedbackReportService feedbackReportService;

    @PostMapping("/api/student/feedback-reports")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','PARENT','ADMIN')")
    public ResponseEntity<FeedbackReportResponseDTO> create(
            Authentication authentication,
            @Valid @RequestBody FeedbackReportRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(feedbackReportService.create(authentication.getName(), dto));
    }

    @GetMapping("/api/admin/feedback-reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FeedbackReportResponseDTO>> findAll() {
        return ResponseEntity.ok(feedbackReportService.findAll());
    }

    @PatchMapping("/api/admin/feedback-reports/{reportId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeedbackReportResponseDTO> updateStatus(
            @PathVariable UUID reportId,
            @Valid @RequestBody FeedbackReportStatusUpdateDTO dto) {
        return ResponseEntity.ok(feedbackReportService.updateStatus(reportId, dto));
    }
}
