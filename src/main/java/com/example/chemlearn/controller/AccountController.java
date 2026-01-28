package com.example.chemlearn.controller;

import com.example.chemlearn.dtos.AccountResponseDTO;
import com.example.chemlearn.dtos.CreateAccountDTO;
import com.example.chemlearn.dtos.UpdateAccountDTO;
import com.example.chemlearn.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public AccountResponseDTO getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public AccountResponseDTO create(
            @Valid @RequestBody CreateAccountDTO dto) {
        return service.create(dto);
    }

    @PatchMapping("/{id}")
    public AccountResponseDTO update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAccountDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
