package com.example.chemlearn.repository;

import com.example.chemlearn.entity.MiniQuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MiniQuizQuestionRepository extends JpaRepository<MiniQuizQuestion, Long> {
    List<MiniQuizQuestion> findByLessonIdOrderByIdAsc(Long lessonId);
}
