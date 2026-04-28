package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.request.StudyClassEnrollmentRequest;
import com.example.chemlearn.lms.dto.response.StudyClassEnrollmentResponse;
import com.example.chemlearn.lms.entity.StudyClassEnrollment;
import com.example.chemlearn.lms.service.StudyClassEnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lms/class-enrollments")
@CrossOrigin(origins = "*")
public class StudyClassEnrollmentController {

    @Autowired
    private StudyClassEnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<StudyClassEnrollmentResponse> create(@RequestBody StudyClassEnrollmentRequest request) {
        StudyClassEnrollment enrollment = new StudyClassEnrollment();
        StudyClassEnrollment created = enrollmentService.create(enrollment);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudyClassEnrollmentResponse> findById(@PathVariable UUID id) {
        return enrollmentService.findById(id)
                .map(enrollment -> ResponseEntity.ok(mapToResponse(enrollment)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<StudyClassEnrollmentResponse>> findAll() {
        List<StudyClassEnrollmentResponse> enrollments = enrollmentService.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<List<StudyClassEnrollmentResponse>> findByClassId(@PathVariable UUID classId) {
        List<StudyClassEnrollmentResponse> enrollments = enrollmentService.findByClassId(classId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudyClassEnrollmentResponse>> findByStudentId(@PathVariable UUID studentId) {
        List<StudyClassEnrollmentResponse> enrollments = enrollmentService.findByStudentId(studentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/class/{classId}/student/{studentId}")
    public ResponseEntity<StudyClassEnrollmentResponse> findByClassIdAndStudentId(
            @PathVariable UUID classId, @PathVariable UUID studentId) {
        return enrollmentService.findByClassIdAndStudentId(classId, studentId)
                .map(enrollment -> ResponseEntity.ok(mapToResponse(enrollment)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudyClassEnrollmentResponse> update(@PathVariable UUID id, @RequestBody StudyClassEnrollmentRequest request) {
        StudyClassEnrollment enrollment = new StudyClassEnrollment();
        StudyClassEnrollment updated = enrollmentService.update(id, enrollment);
        return ResponseEntity.ok(mapToResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        enrollmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private StudyClassEnrollmentResponse mapToResponse(StudyClassEnrollment enrollment) {
        return new StudyClassEnrollmentResponse(
                enrollment.getId(),
                enrollment.getStudyClassField() != null ? enrollment.getStudyClassField().getId() : null,
                enrollment.getStudent() != null ? enrollment.getStudent().getId() : null,
                enrollment.getJoinedAt()
        );
    }
}
