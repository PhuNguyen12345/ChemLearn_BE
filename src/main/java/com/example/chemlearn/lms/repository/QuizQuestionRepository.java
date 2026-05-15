package com.example.chemlearn.lms.repository;

import com.example.chemlearn.lms.entity.QuizQuestion;
import com.example.chemlearn.lms.enums.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, UUID> {
    List<QuizQuestion> findByQuizIdOrderByDisplayOrderAsc(UUID quizId);
    long countByQuizId(UUID quizId);
    boolean existsByQuizIdAndQuestionType(UUID quizId, QuestionType questionType);
}
