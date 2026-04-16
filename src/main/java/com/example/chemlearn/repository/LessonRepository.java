package com.example.chemlearn.repository;

import com.example.chemlearn.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByChapterIdAndPublishedTrueOrderByDisplayOrderAsc(Long chapterId);

    Optional<Lesson> findByIdAndPublishedTrue(Long id);
}
