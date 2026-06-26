package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.core.AccountResponseDTO;
import com.example.chemlearn.lms.dto.core.CreateAccountDTO;
import com.example.chemlearn.lms.dto.core.UpdateAccountDTO;
import com.example.chemlearn.lms.dto.core.UpdateGraduationYearDTO;
import com.example.chemlearn.lms.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor()
public class UserController {

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

    @PatchMapping("/{id}/deactivate")
    public AccountResponseDTO deactivate(@PathVariable UUID id) {
        return service.deactivate(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Admin override: cập nhật target_graduation_year cho học sinh.
     * Dùng cho trường hợp đặc biệt: lưu ban, chuyển trường, điều chỉnh thủ công.
     */
    @PutMapping("/{id}/graduation-year")
    public ResponseEntity<String> updateGraduationYear(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateGraduationYearDTO dto) {
        service.updateGraduationYear(id, dto);
        return ResponseEntity.ok("Graduation year updated successfully");
    }
}

