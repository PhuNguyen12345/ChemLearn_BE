package com.example.chemlearn.lms.repository;

import com.example.chemlearn.lms.entity.QuizAttempt;
import com.example.chemlearn.lms.enums.AttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {
    Optional<QuizAttempt> findByIdAndStudentId(UUID id, UUID studentId);
    Optional<QuizAttempt> findFirstByQuizIdAndStudentIdAndStatusOrderByStartedAtDesc(UUID quizId, UUID studentId, AttemptStatus status);
    List<QuizAttempt> findByQuizCreatedByIdOrderByStartedAtDesc(UUID teacherId);
    List<QuizAttempt> findByStudentIdOrderByStartedAtDesc(UUID studentId);
    List<QuizAttempt> findByQuizIdAndStudentIdOrderByStartedAtDesc(UUID quizId, UUID studentId);
}
