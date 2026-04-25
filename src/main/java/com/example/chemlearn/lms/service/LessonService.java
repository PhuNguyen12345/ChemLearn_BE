package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.entity.Lesson;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonService {
    Lesson create(Lesson lesson);
    Optional<Lesson> findById(UUID id);
    List<Lesson> findAll();
    List<Lesson> findByChapterId(UUID chapterId);
    Lesson update(UUID id, Lesson lesson);
    void delete(UUID id);
}
