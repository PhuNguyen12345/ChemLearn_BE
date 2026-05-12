package com.example.chemlearn.lms.repository;

import com.example.chemlearn.lms.entity.Quiz;
import com.example.chemlearn.lms.enums.QuizType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuizRepository extends JpaRepository<Quiz, UUID> {
    List<Quiz> findByPublishedTrueAndQuizTypeOrderByIdAsc(QuizType quizType);
    List<Quiz> findByCreatedByIdOrderByIdDesc(UUID createdById);
    List<Quiz> findAllByOrderByIdDesc();
    Optional<Quiz> findByIdAndPublishedTrue(UUID id);
}
