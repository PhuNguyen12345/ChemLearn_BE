package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.request.StudyClassAssignmentRequest;
import com.example.chemlearn.lms.dto.response.StudyClassAssignmentResponse;
import com.example.chemlearn.lms.entity.StudyClassAssignment;
import com.example.chemlearn.lms.service.StudyClassAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lms/class-assignments")
@CrossOrigin(origins = "*")
public class StudyClassAssignmentController {

    @Autowired
    private StudyClassAssignmentService assignmentService;

    @PostMapping
    public ResponseEntity<StudyClassAssignmentResponse> create(@RequestBody StudyClassAssignmentRequest request) {
        StudyClassAssignment assignment = new StudyClassAssignment();
        assignment.setTitle(request.getTitle());
        assignment.setDueDate(request.getDueDate());
        StudyClassAssignment created = assignmentService.create(assignment);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudyClassAssignmentResponse> findById(@PathVariable UUID id) {
        return assignmentService.findById(id)
                .map(assignment -> ResponseEntity.ok(mapToResponse(assignment)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<StudyClassAssignmentResponse>> findAll() {
        List<StudyClassAssignmentResponse> assignments = assignmentService.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<List<StudyClassAssignmentResponse>> findByClassId(@PathVariable UUID classId) {
        List<StudyClassAssignmentResponse> assignments = assignmentService.findByClassId(classId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<List<StudyClassAssignmentResponse>> findByQuizId(@PathVariable UUID quizId) {
        List<StudyClassAssignmentResponse> assignments = assignmentService.findByQuizId(quizId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/lab/{labId}")
    public ResponseEntity<List<StudyClassAssignmentResponse>> findByLabId(@PathVariable UUID labId) {
        List<StudyClassAssignmentResponse> assignments = assignmentService.findByLabId(labId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(assignments);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudyClassAssignmentResponse> update(@PathVariable UUID id, @RequestBody StudyClassAssignmentRequest request) {
        StudyClassAssignment assignment = new StudyClassAssignment();
        assignment.setTitle(request.getTitle());
        assignment.setDueDate(request.getDueDate());
        StudyClassAssignment updated = assignmentService.update(id, assignment);
        return ResponseEntity.ok(mapToResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        assignmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private StudyClassAssignmentResponse mapToResponse(StudyClassAssignment assignment) {
        return new StudyClassAssignmentResponse(
                assignment.getId(),
                assignment.getStudyClassField() != null ? assignment.getStudyClassField().getId() : null,
                assignment.getTitle(),
                assignment.getLab() != null ? assignment.getLab().getId() : null,
                assignment.getQuiz() != null ? assignment.getQuiz().getId() : null,
                assignment.getDueDate(),
                assignment.getCreatedAt()
        );
    }
}
