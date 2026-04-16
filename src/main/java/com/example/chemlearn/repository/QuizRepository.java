package com.example.chemlearn.repository;

import com.example.chemlearn.entity.Quiz;
import com.example.chemlearn.enums.QuizType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByPublishedTrueAndQuizTypeOrderByIdAsc(QuizType quizType);

    List<Quiz> findByCreatedByIdOrderByIdDesc(Long createdById);

    Optional<Quiz> findByIdAndPublishedTrue(Long id);
}
