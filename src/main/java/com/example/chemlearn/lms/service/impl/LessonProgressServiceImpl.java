package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.lms.entity.LessonProgress;
import com.example.chemlearn.lms.repository.LessonProgressRepository;
import com.example.chemlearn.lms.service.LessonProgressService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LessonProgressServiceImpl implements LessonProgressService {
    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Override
    public LessonProgress create(LessonProgress progress) {
        progress.setLastAccessedAt(Instant.now());
        if (progress.getIsCompleted() == null) progress.setIsCompleted(false);
        if (progress.getIsLocked() == null) progress.setIsLocked(true);
        return lessonProgressRepository.save(progress);
    }

    @Override
    public Optional<LessonProgress> findById(UUID id) {
        return lessonProgressRepository.findAll().stream().filter(progress -> id.equals(progress.getId())).findFirst();
    }

    @Override
    public List<LessonProgress> findAll() {
        return lessonProgressRepository.findAll();
    }

    @Override
    public List<LessonProgress> findByStudentId(UUID studentId) {
        return lessonProgressRepository.findByStudentId(studentId);
    }

    @Override
    public Optional<LessonProgress> findByStudentIdAndLessonId(UUID studentId, UUID lessonId) {
        return lessonProgressRepository.findByStudentIdAndLessonId(studentId, lessonId);
    }

    @Override
    public List<LessonProgress> findCompletedLessons(UUID studentId) {
        return lessonProgressRepository.findByStudentIdAndIsCompleted(studentId, true);
    }

    @Override
    public List<LessonProgress> findLockedLessons(UUID studentId) {
        return lessonProgressRepository.findByStudentIdAndIsLocked(studentId, true);
    }

    @Override
    public LessonProgress update(UUID id, LessonProgress progress) {
        LessonProgress existing = findById(id).orElseThrow(() -> new RuntimeException("LessonProgress not found"));
        if (progress.getIsCompleted() != null) existing.setIsCompleted(progress.getIsCompleted());
        if (progress.getIsLocked() != null) existing.setIsLocked(progress.getIsLocked());
        existing.setLastAccessedAt(Instant.now());
        return lessonProgressRepository.save(existing);
    }

    @Override
    public void delete(UUID id) {
        findById(id).ifPresent(lessonProgressRepository::delete);
    }
}
