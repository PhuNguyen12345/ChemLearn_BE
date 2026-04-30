package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.request.StudyClassAssignmentRequest;
import com.example.chemlearn.lms.dto.response.StudyClassAssignmentResponse;
import com.example.chemlearn.lms.entity.StudyClassAssignment;
import com.example.chemlearn.lms.service.StudyClassAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lms/class-assignments")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('TEACHER')")
public class StudyClassAssignmentController {

    @Autowired
    private StudyClassAssignmentService assignmentService;

    @PostMapping
    public ResponseEntity<StudyClassAssignmentResponse> create(@RequestBody StudyClassAssignmentRequest request,
                                                               Authentication authentication) {
        StudyClassAssignment assignment = new StudyClassAssignment();
        assignment.setStudyClassField(new com.example.chemlearn.lms.entity.StudyClass());
        assignment.getStudyClassField().setId(request.getClassId());
        assignment.setTitle(request.getTitle());
        if (request.getQuizId() != null) {
            assignment.setQuiz(new com.example.chemlearn.lms.entity.Quiz());
            assignment.getQuiz().setId(request.getQuizId());
        }
        if (request.getLabId() != null) {
            assignment.setLab(new com.example.chemlearn.lab.entity.Lab());
            assignment.getLab().setId(request.getLabId());
        }
        assignment.setDueDate(request.getDueDate());
        StudyClassAssignment created = assignmentService.create(assignment, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudyClassAssignmentResponse> findById(@PathVariable UUID id, Authentication authentication) {
        return assignmentService.findById(id, authentication.getName())
                .map(assignment -> ResponseEntity.ok(mapToResponse(assignment)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<StudyClassAssignmentResponse>> findAll(Authentication authentication) {
        List<StudyClassAssignmentResponse> assignments = assignmentService.findAll(authentication.getName())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<List<StudyClassAssignmentResponse>> findByClassId(@PathVariable UUID classId, Authentication authentication) {
        List<StudyClassAssignmentResponse> assignments = assignmentService.findByClassId(classId, authentication.getName())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<List<StudyClassAssignmentResponse>> findByQuizId(@PathVariable UUID quizId, Authentication authentication) {
        List<StudyClassAssignmentResponse> assignments = assignmentService.findByQuizId(quizId, authentication.getName())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/lab/{labId}")
    public ResponseEntity<List<StudyClassAssignmentResponse>> findByLabId(@PathVariable UUID labId, Authentication authentication) {
        List<StudyClassAssignmentResponse> assignments = assignmentService.findByLabId(labId, authentication.getName())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(assignments);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudyClassAssignmentResponse> update(@PathVariable UUID id,
                                                               @RequestBody StudyClassAssignmentRequest request,
                                                               Authentication authentication) {
        StudyClassAssignment assignment = new StudyClassAssignment();
        assignment.setStudyClassField(new com.example.chemlearn.lms.entity.StudyClass());
        assignment.getStudyClassField().setId(request.getClassId());
        assignment.setTitle(request.getTitle());
        if (request.getQuizId() != null) {
            assignment.setQuiz(new com.example.chemlearn.lms.entity.Quiz());
            assignment.getQuiz().setId(request.getQuizId());
        }
        if (request.getLabId() != null) {
            assignment.setLab(new com.example.chemlearn.lab.entity.Lab());
            assignment.getLab().setId(request.getLabId());
        }
        assignment.setDueDate(request.getDueDate());
        StudyClassAssignment updated = assignmentService.update(id, assignment, authentication.getName());
        return ResponseEntity.ok(mapToResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
        assignmentService.delete(id, authentication.getName());
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
