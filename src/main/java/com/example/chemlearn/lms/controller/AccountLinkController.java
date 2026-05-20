package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.core.AccountLinkConfirmDTO;
import com.example.chemlearn.lms.dto.core.AccountLinkInitiateDTO;
import com.example.chemlearn.lms.entity.AccountLinkRequest;
import com.example.chemlearn.lms.service.AccountLinkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account-link")
@RequiredArgsConstructor
public class AccountLinkController {

    private final AccountLinkService accountLinkService;

    @PostMapping("/initiate")
    public ResponseEntity<?> initiateLink(@Valid @RequestBody AccountLinkInitiateDTO dto, Authentication authentication) {
        accountLinkService.initiateLink(authentication.getName(), dto);
        return ResponseEntity.ok().body("{\"message\":\"Yêu cầu liên kết đã được gửi tới email đích.\"}");
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmLink(@Valid @RequestBody AccountLinkConfirmDTO dto) {
        accountLinkService.confirmLink(dto);
        return ResponseEntity.ok().body("{\"message\":\"Liên kết tài khoản thành công!\"}");
    }

    @GetMapping("/pending")
    public ResponseEntity<List<AccountLinkRequest>> getPendingLinks(Authentication authentication) {
        return ResponseEntity.ok(accountLinkService.getPendingLinks(authentication.getName()));
    }
}
