package com.example.chemlearn.repository;

import com.example.chemlearn.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByQuizIdOrderByDisplayOrderAsc(Long quizId);

    long countByQuizId(Long quizId);
}
