package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.admin.*;
import com.example.chemlearn.lms.service.AdminContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/content")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminContentController {

    private final AdminContentService adminContentService;

    // ==================== CHAPTER ENDPOINTS ====================

    /**
     * Get all chapters (admin view)
     */
    @GetMapping("/chapters")
    public ResponseEntity<List<AdminChapterResponseDTO>> getAllChapters() {
        List<AdminChapterResponseDTO> chapters = adminContentService.getAllChapters();
        return ResponseEntity.ok(chapters);
    }

    /**
     * Get chapter by ID
     */
    @GetMapping("/chapters/{chapterId}")
    public ResponseEntity<AdminChapterResponseDTO> getChapterById(@PathVariable UUID chapterId) {
        AdminChapterResponseDTO chapter = adminContentService.getChapterById(chapterId);
        return ResponseEntity.ok(chapter);
    }

    /**
     * Create new chapter
     */
    @PostMapping("/chapters")
    public ResponseEntity<AdminChapterResponseDTO> createChapter(
            @Valid @RequestBody AdminChapterRequestDTO dto,
            Authentication authentication) {
        AdminChapterResponseDTO chapter = adminContentService.createChapter(dto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(chapter);
    }

    /**
     * Update existing chapter
     */
    @PutMapping("/chapters/{chapterId}")
    public ResponseEntity<AdminChapterResponseDTO> updateChapter(
            @PathVariable UUID chapterId,
            @Valid @RequestBody AdminChapterRequestDTO dto,
            Authentication authentication) {
        AdminChapterResponseDTO chapter = adminContentService.updateChapter(chapterId, dto, authentication.getName());
        return ResponseEntity.ok(chapter);
    }

    /**
     * Delete chapter
     */
    @DeleteMapping("/chapters/{chapterId}")
    public ResponseEntity<Void> deleteChapter(
            @PathVariable UUID chapterId,
            Authentication authentication) {
        adminContentService.deleteChapter(chapterId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    // ==================== LESSON ENDPOINTS ====================

    /**
     * Get all lessons for a chapter
     */
    @GetMapping("/chapters/{chapterId}/lessons")
    public ResponseEntity<List<AdminLessonResponseDTO>> getLessonsByChapter(@PathVariable UUID chapterId) {
        List<AdminLessonResponseDTO> lessons = adminContentService.getLessonsByChapter(chapterId);
        return ResponseEntity.ok(lessons);
    }

    /**
     * Get lesson by ID
     */
    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<AdminLessonResponseDTO> getLessonById(@PathVariable UUID lessonId) {
        AdminLessonResponseDTO lesson = adminContentService.getLessonById(lessonId);
        return ResponseEntity.ok(lesson);
    }

    /**
     * Create new lesson
     */
    @PostMapping("/lessons")
    public ResponseEntity<AdminLessonResponseDTO> createLesson(
            @Valid @RequestBody AdminLessonRequestDTO dto,
            Authentication authentication) {
        AdminLessonResponseDTO lesson = adminContentService.createLesson(dto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(lesson);
    }

    /**
     * Update existing lesson
     */
    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<AdminLessonResponseDTO> updateLesson(
            @PathVariable UUID lessonId,
            @Valid @RequestBody AdminLessonRequestDTO dto,
            Authentication authentication) {
        AdminLessonResponseDTO lesson = adminContentService.updateLesson(lessonId, dto, authentication.getName());
        return ResponseEntity.ok(lesson);
    }

    /**
     * Delete lesson
     */
    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<Void> deleteLesson(
            @PathVariable UUID lessonId,
            Authentication authentication) {
        adminContentService.deleteLesson(lessonId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    // ==================== MINI-QUIZ QUESTION ENDPOINTS ====================

    /**
     * Get all mini-quiz questions for a lesson
     */
    @GetMapping("/lessons/{lessonId}/mini-quiz-questions")
    public ResponseEntity<List<AdminMiniQuizQuestionDTO>> getMiniQuizQuestionsByLesson(@PathVariable UUID lessonId) {
        List<AdminMiniQuizQuestionDTO> questions = adminContentService.getMiniQuizQuestionsByLesson(lessonId);
        return ResponseEntity.ok(questions);
    }

    /**
     * Add mini-quiz question to a lesson
     */
    @PostMapping("/lessons/{lessonId}/mini-quiz-questions")
    public ResponseEntity<AdminMiniQuizQuestionDTO> addMiniQuizQuestion(
            @PathVariable UUID lessonId,
            @Valid @RequestBody AdminMiniQuizQuestionDTO dto,
            Authentication authentication) {
        AdminMiniQuizQuestionDTO question = adminContentService.addMiniQuizQuestion(lessonId, dto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(question);
    }

    /**
     * Update mini-quiz question
     */
    @PutMapping("/mini-quiz-questions/{questionId}")
    public ResponseEntity<AdminMiniQuizQuestionDTO> updateMiniQuizQuestion(
            @PathVariable UUID questionId,
            @Valid @RequestBody AdminMiniQuizQuestionDTO dto,
            Authentication authentication) {
        AdminMiniQuizQuestionDTO question = adminContentService.updateMiniQuizQuestion(questionId, dto, authentication.getName());
        return ResponseEntity.ok(question);
    }

    /**
     * Delete mini-quiz question
     */
    @DeleteMapping("/mini-quiz-questions/{questionId}")
    public ResponseEntity<Void> deleteMiniQuizQuestion(
            @PathVariable UUID questionId,
            Authentication authentication) {
        adminContentService.deleteMiniQuizQuestion(questionId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
