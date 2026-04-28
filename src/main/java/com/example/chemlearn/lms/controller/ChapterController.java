package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.request.ChapterRequest;
import com.example.chemlearn.lms.dto.response.ChapterResponse;
import com.example.chemlearn.lms.entity.Chapter;
import com.example.chemlearn.lms.service.ChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lms/chapters")
@CrossOrigin(origins = "*")
public class ChapterController {

    @Autowired
    private ChapterService chapterService;

    @PostMapping
    public ResponseEntity<ChapterResponse> create(@RequestBody ChapterRequest request) {
        Chapter chapter = new Chapter();
        chapter.setTitle(request.getTitle());
        chapter.setDescription(request.getDescription());
        chapter.setGradeLevel(request.getGradeLevel());
        chapter.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0);
        Chapter created = chapterService.create(chapter);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChapterResponse> findById(@PathVariable UUID id) {
        return chapterService.findById(id)
                .map(chapter -> ResponseEntity.ok(mapToResponse(chapter)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ChapterResponse>> findAll() {
        List<ChapterResponse> chapters = chapterService.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(chapters);
    }

    @GetMapping("/grade/{gradeLevel}")
    public ResponseEntity<List<ChapterResponse>> findByGradeLevel(@PathVariable Integer gradeLevel) {
        List<ChapterResponse> chapters = chapterService.findByGradeLevel(gradeLevel)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(chapters);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChapterResponse> update(@PathVariable UUID id, @RequestBody ChapterRequest request) {
        Chapter chapter = new Chapter();
        chapter.setTitle(request.getTitle());
        chapter.setDescription(request.getDescription());
        chapter.setGradeLevel(request.getGradeLevel());
        chapter.setOrderIndex(request.getOrderIndex());
        Chapter updated = chapterService.update(id, chapter);
        return ResponseEntity.ok(mapToResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        chapterService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private ChapterResponse mapToResponse(Chapter chapter) {
        return new ChapterResponse(
                chapter.getId(),
                chapter.getTitle(),
                chapter.getDescription(),
                chapter.getGradeLevel(),
                chapter.getOrderIndex(),
                chapter.getCreatedAt(),
                chapter.getUpdatedAt()
        );
    }
}
