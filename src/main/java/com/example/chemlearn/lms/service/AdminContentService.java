package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.dto.admin.AdminChapterRequestDTO;
import com.example.chemlearn.lms.dto.admin.AdminChapterResponseDTO;
import com.example.chemlearn.lms.dto.admin.AdminLessonRequestDTO;
import com.example.chemlearn.lms.dto.admin.AdminLessonResponseDTO;
import com.example.chemlearn.lms.dto.admin.AdminMiniQuizQuestionDTO;

import java.util.List;
import java.util.UUID;

public interface AdminContentService {
    // Chapter operations
    List<AdminChapterResponseDTO> getAllChapters();

    AdminChapterResponseDTO getChapterById(UUID chapterId);

    AdminChapterResponseDTO createChapter(AdminChapterRequestDTO dto, String adminUsername);

    AdminChapterResponseDTO updateChapter(UUID chapterId, AdminChapterRequestDTO dto, String adminUsername);

    void deleteChapter(UUID chapterId, String adminUsername);

    // Lesson operations
    List<AdminLessonResponseDTO> getLessonsByChapter(UUID chapterId);

    AdminLessonResponseDTO getLessonById(UUID lessonId);

    AdminLessonResponseDTO createLesson(AdminLessonRequestDTO dto, String adminUsername);

    AdminLessonResponseDTO updateLesson(UUID lessonId, AdminLessonRequestDTO dto, String adminUsername);

    void deleteLesson(UUID lessonId, String adminUsername);

    // Mini-quiz operations
    List<AdminMiniQuizQuestionDTO> getMiniQuizQuestionsByLesson(UUID lessonId);

    AdminMiniQuizQuestionDTO addMiniQuizQuestion(UUID lessonId, AdminMiniQuizQuestionDTO dto, String adminUsername);

    AdminMiniQuizQuestionDTO updateMiniQuizQuestion(UUID questionId, AdminMiniQuizQuestionDTO dto, String adminUsername);

    void deleteMiniQuizQuestion(UUID questionId, String adminUsername);
}
