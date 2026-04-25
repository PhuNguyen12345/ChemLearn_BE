package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.request.LessonRequest;
import com.example.chemlearn.lms.dto.response.LessonResponse;
import com.example.chemlearn.lms.entity.Lesson;
import com.example.chemlearn.lms.service.LessonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lms/lessons")
@CrossOrigin(origins = "*")
public class LessonController {

    @Autowired
    private LessonService lessonService;

    @PostMapping
    public ResponseEntity<LessonResponse> create(@RequestBody LessonRequest request) {
        Lesson lesson = new Lesson();
        lesson.setTitle(request.getTitle());
        lesson.setContentType(request.getContentType());
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setTextContent(request.getTextContent());
        lesson.setDurationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 0);
        lesson.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0);
        Lesson created = lessonService.create(lesson);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonResponse> findById(@PathVariable UUID id) {
        return lessonService.findById(id)
                .map(lesson -> ResponseEntity.ok(mapToResponse(lesson)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<LessonResponse>> findAll() {
        List<LessonResponse> lessons = lessonService.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lessons);
    }

    @GetMapping("/chapter/{chapterId}")
    public ResponseEntity<List<LessonResponse>> findByChapterId(@PathVariable UUID chapterId) {
        List<LessonResponse> lessons = lessonService.findByChapterId(chapterId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lessons);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LessonResponse> update(@PathVariable UUID id, @RequestBody LessonRequest request) {
        Lesson lesson = new Lesson();
        lesson.setTitle(request.getTitle());
        lesson.setContentType(request.getContentType());
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setTextContent(request.getTextContent());
        lesson.setDurationMinutes(request.getDurationMinutes());
        lesson.setOrderIndex(request.getOrderIndex());
        Lesson updated = lessonService.update(id, lesson);
        return ResponseEntity.ok(mapToResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        lessonService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private LessonResponse mapToResponse(Lesson lesson) {
        return new LessonResponse(
                lesson.getId(),
                lesson.getChapter() != null ? lesson.getChapter().getId() : null,
                lesson.getLab() != null ? lesson.getLab().getId() : null,
                lesson.getTitle(),
                lesson.getContentType(),
                lesson.getVideoUrl(),
                lesson.getTextContent(),
                lesson.getDurationMinutes(),
                lesson.getOrderIndex(),
                lesson.getCreatedAt(),
                lesson.getUpdatedAt()
        );
    }
}
