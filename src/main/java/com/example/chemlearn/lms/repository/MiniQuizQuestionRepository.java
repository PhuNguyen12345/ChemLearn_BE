package com.example.chemlearn.lms.repository;

import com.example.chemlearn.lms.entity.MiniQuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MiniQuizQuestionRepository extends JpaRepository<MiniQuizQuestion, UUID> {
    List<MiniQuizQuestion> findByLessonIdOrderByIdAsc(UUID lessonId);
}
