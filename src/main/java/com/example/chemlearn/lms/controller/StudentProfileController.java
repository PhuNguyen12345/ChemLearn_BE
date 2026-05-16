package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.student.ChangePasswordDTO;
import com.example.chemlearn.lms.dto.student.StudentProfileDTO;
import com.example.chemlearn.lms.dto.student.UpdateProfileDTO;
import com.example.chemlearn.lms.service.StudentProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/profile")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    @GetMapping
    public ResponseEntity<StudentProfileDTO> getProfile() {
        return ResponseEntity.ok(studentProfileService.getProfile());
    }

    @PutMapping
    public ResponseEntity<StudentProfileDTO> updateProfile(@Valid @RequestBody UpdateProfileDTO dto) {
        return ResponseEntity.ok(studentProfileService.updateProfile(dto));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        studentProfileService.changePassword(dto);
        return ResponseEntity.ok().build();
    }
}
