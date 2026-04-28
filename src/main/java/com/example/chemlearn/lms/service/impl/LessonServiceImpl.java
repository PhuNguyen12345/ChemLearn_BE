package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.lms.entity.Lesson;
import com.example.chemlearn.lms.repository.LessonRepository;
import com.example.chemlearn.lms.service.LessonService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LessonServiceImpl implements LessonService {
    @Autowired
    private LessonRepository lessonRepository;

    @Override
    public Lesson create(Lesson lesson) {
        lesson.setCreatedAt(Instant.now());
        lesson.setUpdatedAt(Instant.now());
        return lessonRepository.save(lesson);
    }

    @Override
    public Optional<Lesson> findById(UUID id) {
        return lessonRepository.findAll().stream().filter(lesson -> id.equals(lesson.getId())).findFirst();
    }

    @Override
    public List<Lesson> findAll() {
        return lessonRepository.findAll();
    }

    @Override
    public List<Lesson> findByChapterId(UUID chapterId) {
        return lessonRepository.findAll().stream().filter(lesson -> lesson.getChapter() != null && chapterId.equals(lesson.getChapter().getId())).toList();
    }

    @Override
    public Lesson update(UUID id, Lesson lesson) {
        Lesson existing = findById(id).orElseThrow(() -> new RuntimeException("Lesson not found"));
        if (lesson.getChapter() != null) existing.setChapter(lesson.getChapter());
        if (lesson.getLab() != null) existing.setLab(lesson.getLab());
        if (lesson.getTitle() != null) existing.setTitle(lesson.getTitle());
        if (lesson.getContentType() != null) existing.setContentType(lesson.getContentType());
        if (lesson.getVideoUrl() != null) existing.setVideoUrl(lesson.getVideoUrl());
        if (lesson.getTextContent() != null) existing.setTextContent(lesson.getTextContent());
        if (lesson.getDurationMinutes() != null) existing.setDurationMinutes(lesson.getDurationMinutes());
        if (lesson.getOrderIndex() != null) existing.setOrderIndex(lesson.getOrderIndex());
        existing.setUpdatedAt(Instant.now());
        return lessonRepository.save(existing);
    }

    @Override
    public void delete(UUID id) {
        findById(id).ifPresent(lessonRepository::delete);
    }
}
