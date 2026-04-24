package com.example.chemlearn.repository;

import com.example.chemlearn.entity.QuizAttempt;
import com.example.chemlearn.enums.AttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    Optional<QuizAttempt> findByIdAndStudentId(Long id, Long studentId);

    Optional<QuizAttempt> findFirstByQuizIdAndStudentIdAndStatusOrderByStartedAtDesc(Long quizId, Long studentId, AttemptStatus status);

    List<QuizAttempt> findByQuizCreatedByIdOrderByStartedAtDesc(Long teacherId);

    List<QuizAttempt> findByStudentIdOrderByStartedAtDesc(Long studentId);
}
