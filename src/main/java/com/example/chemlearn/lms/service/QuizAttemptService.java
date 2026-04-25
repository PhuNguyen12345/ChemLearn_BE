package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.entity.QuizAttempt;
import com.example.chemlearn.lms.enums.AttemptStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuizAttemptService {
    QuizAttempt create(QuizAttempt attempt);
    Optional<QuizAttempt> findById(UUID id);
    List<QuizAttempt> findAll();
    List<QuizAttempt> findByStudentId(UUID studentId);
    List<QuizAttempt> findByQuizId(UUID quizId);
    List<QuizAttempt> findByLessonId(UUID lessonId);
    List<QuizAttempt> findByStudentIdAndStatus(UUID studentId, AttemptStatus status);
    List<QuizAttempt> findByStudentIdAndQuizId(UUID studentId, UUID quizId);
    QuizAttempt update(UUID id, QuizAttempt attempt);
    void delete(UUID id);
}
