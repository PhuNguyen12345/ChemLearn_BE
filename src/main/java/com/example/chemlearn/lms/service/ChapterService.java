package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.entity.Chapter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChapterService {
    Chapter create(Chapter chapter);
    Optional<Chapter> findById(UUID id);
    List<Chapter> findAll();
    List<Chapter> findByGradeLevel(Integer gradeLevel);
    Chapter update(UUID id, Chapter chapter);
    void delete(UUID id);
}
