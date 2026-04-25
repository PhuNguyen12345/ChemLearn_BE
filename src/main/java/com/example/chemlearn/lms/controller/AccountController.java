package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.core.AccountResponseDTO;
import com.example.chemlearn.lms.dto.core.CreateAccountDTO;
import com.example.chemlearn.lms.dto.core.UpdateAccountDTO;
import com.example.chemlearn.lms.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor()
public class AccountController {

    private final AccountService service;

    @GetMapping
    public List<AccountResponseDTO> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public AccountResponseDTO getById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PostMapping
    public AccountResponseDTO create(
            @Valid @RequestBody CreateAccountDTO dto) {
        return service.create(dto);
    }

    @PatchMapping("/{id}")
    public AccountResponseDTO update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAccountDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

