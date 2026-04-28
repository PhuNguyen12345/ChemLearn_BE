package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.request.LessonProgressRequest;
import com.example.chemlearn.lms.dto.response.LessonProgressResponse;
import com.example.chemlearn.lms.entity.LessonProgress;
import com.example.chemlearn.lms.service.LessonProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lms/lesson-progress")
@CrossOrigin(origins = "*")
public class LessonProgressController {

    @Autowired
    private LessonProgressService lessonProgressService;

    @PostMapping
    public ResponseEntity<LessonProgressResponse> create(@RequestBody LessonProgressRequest request) {
        LessonProgress progress = new LessonProgress();
        progress.setIsCompleted(request.getIsCompleted() != null ? request.getIsCompleted() : false);
        progress.setIsLocked(request.getIsLocked() != null ? request.getIsLocked() : true);
        LessonProgress created = lessonProgressService.create(progress);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonProgressResponse> findById(@PathVariable UUID id) {
        return lessonProgressService.findById(id)
                .map(progress -> ResponseEntity.ok(mapToResponse(progress)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<LessonProgressResponse>> findAll() {
        List<LessonProgressResponse> progressList = lessonProgressService.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(progressList);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<LessonProgressResponse>> findByStudentId(@PathVariable UUID studentId) {
        List<LessonProgressResponse> progressList = lessonProgressService.findByStudentId(studentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(progressList);
    }

    @GetMapping("/student/{studentId}/lesson/{lessonId}")
    public ResponseEntity<LessonProgressResponse> findByStudentIdAndLessonId(
            @PathVariable UUID studentId, @PathVariable UUID lessonId) {
        return lessonProgressService.findByStudentIdAndLessonId(studentId, lessonId)
                .map(progress -> ResponseEntity.ok(mapToResponse(progress)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}/completed")
    public ResponseEntity<List<LessonProgressResponse>> findCompletedLessons(@PathVariable UUID studentId) {
        List<LessonProgressResponse> progressList = lessonProgressService.findCompletedLessons(studentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(progressList);
    }

    @GetMapping("/student/{studentId}/locked")
    public ResponseEntity<List<LessonProgressResponse>> findLockedLessons(@PathVariable UUID studentId) {
        List<LessonProgressResponse> progressList = lessonProgressService.findLockedLessons(studentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(progressList);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LessonProgressResponse> update(@PathVariable UUID id, @RequestBody LessonProgressRequest request) {
        LessonProgress progress = new LessonProgress();
        progress.setIsCompleted(request.getIsCompleted());
        progress.setIsLocked(request.getIsLocked());
        LessonProgress updated = lessonProgressService.update(id, progress);
        return ResponseEntity.ok(mapToResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        lessonProgressService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private LessonProgressResponse mapToResponse(LessonProgress progress) {
        return new LessonProgressResponse(
                progress.getId(),
                progress.getStudent() != null ? progress.getStudent().getId() : null,
                progress.getLesson() != null ? progress.getLesson().getId() : null,
                progress.getIsCompleted(),
                progress.getIsLocked(),
                progress.getLastAccessedAt()
        );
    }
}
