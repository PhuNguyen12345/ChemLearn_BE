package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.admin.AdminClassRequestDTO;
import com.example.chemlearn.lms.dto.admin.AdminClassResponseDTO;
import com.example.chemlearn.lms.service.AdminClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/classes")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminClassController {

    private final AdminClassService adminClassService;

    @GetMapping
    public List<AdminClassResponseDTO> getClasses() {
        return adminClassService.getClasses();
    }

    @PostMapping
    public AdminClassResponseDTO createClass(@Valid @RequestBody AdminClassRequestDTO dto) {
        return adminClassService.createClass(dto);
    }

    @PutMapping("/{classId}")
    public AdminClassResponseDTO updateClass(@PathVariable UUID classId, @Valid @RequestBody AdminClassRequestDTO dto) {
        return adminClassService.updateClass(classId, dto);
    }

    @DeleteMapping("/{classId}")
    public ResponseEntity<Void> deleteClass(@PathVariable UUID classId) {
        adminClassService.deleteClass(classId);
        return ResponseEntity.noContent().build();
    }
}

