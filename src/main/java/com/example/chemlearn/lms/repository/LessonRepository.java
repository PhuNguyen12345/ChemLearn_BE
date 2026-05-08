package com.example.chemlearn.lms.repository;

import com.example.chemlearn.lms.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    List<Lesson> findByChapterIdAndPublishedTrueOrderByOrderIndexAsc(UUID chapterId);
    Optional<Lesson> findByIdAndPublishedTrue(UUID id);
    List<Lesson> findByChapterIdOrderByOrderIndexAsc(UUID chapterId);
    int countByChapterId(UUID chapterId);
}
