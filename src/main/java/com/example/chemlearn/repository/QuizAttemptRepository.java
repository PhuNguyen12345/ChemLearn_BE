package com.example.chemlearn.repository;

import com.example.chemlearn.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    Optional<QuizAttempt> findByIdAndStudentId(Long id, Long studentId);

    List<QuizAttempt> findByQuizCreatedByIdOrderByStartedAtDesc(Long teacherId);

    List<QuizAttempt> findByStudentIdOrderByStartedAtDesc(Long studentId);
}
