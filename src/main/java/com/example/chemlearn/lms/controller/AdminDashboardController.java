package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.admin.AdminDashboardSummaryDTO;
import com.example.chemlearn.lms.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final AdminDashboardService adminDashboardService;

    @GetMapping("/summary")
    public ResponseEntity<AdminDashboardSummaryDTO> getSummary() {
        return ResponseEntity.ok(adminDashboardService.getSummary());
    }
}
