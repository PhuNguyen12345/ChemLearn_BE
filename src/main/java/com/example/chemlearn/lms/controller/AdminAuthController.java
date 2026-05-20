package com.example.chemlearn.lms.controller;

import com.example.chemlearn.core.entity.AccessRequest;
import com.example.chemlearn.core.entity.Invite;
import com.example.chemlearn.lms.dto.core.auth.InviteCreateRequestDTO;
import com.example.chemlearn.lms.service.AuthOnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/auth")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AuthOnboardingService onboardingService;

    @GetMapping("/requests")
    public ResponseEntity<List<AccessRequest>> getAccessRequests() {
        return ResponseEntity.ok(onboardingService.getAllRequests());
    }

    @PostMapping("/requests/{requestId}/approve")
    public ResponseEntity<String> approveAccessRequest(@PathVariable UUID requestId) {
        onboardingService.approveRequest(requestId);
        return ResponseEntity.ok("Request approved and invite generated");
    }

    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<String> rejectAccessRequest(@PathVariable UUID requestId) {
        onboardingService.rejectRequest(requestId);
        return ResponseEntity.ok("Request rejected");
    }

    @GetMapping("/invites")
    public ResponseEntity<List<Invite>> getInvites() {
        return ResponseEntity.ok(onboardingService.getAllInvites());
    }

    @PostMapping("/invites")
    public ResponseEntity<Invite> createInvite(@Valid @RequestBody InviteCreateRequestDTO dto) {
        Invite invite = onboardingService.createInvite(dto.getEmail(), dto.getRole());
        return ResponseEntity.ok(invite);
    }
}
