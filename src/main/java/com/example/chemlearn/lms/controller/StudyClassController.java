package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.request.StudyClassRequest;
import com.example.chemlearn.lms.dto.response.StudyClassResponse;
import com.example.chemlearn.lms.entity.StudyClass;
import com.example.chemlearn.lms.service.StudyClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lms/classes")
@CrossOrigin(origins = "*")
public class StudyClassController {

    @Autowired
    private StudyClassService studyClassService;

    @PostMapping
    public ResponseEntity<StudyClassResponse> create(@RequestBody StudyClassRequest request) {
        StudyClass studyClass = new StudyClass();
        studyClass.setName(request.getName());
        studyClass.setGradeLevel(request.getGradeLevel());
        StudyClass created = studyClassService.create(studyClass);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudyClassResponse> findById(@PathVariable UUID id) {
        return studyClassService.findById(id)
                .map(studyClass -> ResponseEntity.ok(mapToResponse(studyClass)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<StudyClassResponse>> findAll() {
        List<StudyClassResponse> classes = studyClassService.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<StudyClassResponse>> findByTeacherId(@PathVariable UUID teacherId) {
        List<StudyClassResponse> classes = studyClassService.findByTeacherId(teacherId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/grade/{gradeLevel}")
    public ResponseEntity<List<StudyClassResponse>> findByGradeLevel(@PathVariable Integer gradeLevel) {
        List<StudyClassResponse> classes = studyClassService.findByGradeLevel(gradeLevel)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(classes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudyClassResponse> update(@PathVariable UUID id, @RequestBody StudyClassRequest request) {
        StudyClass studyClass = new StudyClass();
        studyClass.setName(request.getName());
        studyClass.setGradeLevel(request.getGradeLevel());
        StudyClass updated = studyClassService.update(id, studyClass);
        return ResponseEntity.ok(mapToResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        studyClassService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private StudyClassResponse mapToResponse(StudyClass studyClass) {
        return new StudyClassResponse(
                studyClass.getId(),
                studyClass.getName(),
            studyClass.getClassCode(),
                studyClass.getGradeLevel(),
                studyClass.getTeacher() != null ? studyClass.getTeacher().getId() : null,
                studyClass.getCreatedAt(),
                studyClass.getUpdatedAt()
        );
    }
}
