package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.entity.LessonProgress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonProgressService {
    LessonProgress create(LessonProgress progress);
    Optional<LessonProgress> findById(UUID id);
    List<LessonProgress> findAll();
    List<LessonProgress> findByStudentId(UUID studentId);
    Optional<LessonProgress> findByStudentIdAndLessonId(UUID studentId, UUID lessonId);
    List<LessonProgress> findCompletedLessons(UUID studentId);
    List<LessonProgress> findLockedLessons(UUID studentId);
    LessonProgress update(UUID id, LessonProgress progress);
    void delete(UUID id);
}
