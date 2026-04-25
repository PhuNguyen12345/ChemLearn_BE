package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.lms.entity.QuizAttempt;
import com.example.chemlearn.lms.enums.AttemptStatus;
import com.example.chemlearn.lms.repository.QuizAttemptRepository;
import com.example.chemlearn.lms.service.QuizAttemptService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuizAttemptServiceImpl implements QuizAttemptService {
    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Override
    public QuizAttempt create(QuizAttempt attempt) {
        attempt.setStartedAt(Instant.now());
        if (attempt.getStatus() == null) {
            attempt.setStatus(AttemptStatus.IN_PROGRESS);
        }
        return quizAttemptRepository.save(attempt);
    }

    @Override
    public Optional<QuizAttempt> findById(UUID id) {
        return quizAttemptRepository.findAll().stream().filter(attempt -> id.equals(attempt.getId())).findFirst();
    }

    @Override
    public List<QuizAttempt> findAll() {
        return quizAttemptRepository.findAll();
    }

    @Override
    public List<QuizAttempt> findByStudentId(UUID studentId) {
        return quizAttemptRepository.findAll().stream().filter(attempt -> attempt.getStudent() != null && studentId.equals(attempt.getStudent().getId())).toList();
    }

    @Override
    public List<QuizAttempt> findByQuizId(UUID quizId) {
        return quizAttemptRepository.findAll().stream().filter(attempt -> attempt.getQuiz() != null && quizId.equals(attempt.getQuiz().getId())).toList();
    }

    @Override
    public List<QuizAttempt> findByLessonId(UUID lessonId) {
        return quizAttemptRepository.findAll().stream().filter(attempt -> attempt.getLesson() != null && lessonId.equals(attempt.getLesson().getId())).toList();
    }

    @Override
    public List<QuizAttempt> findByStudentIdAndStatus(UUID studentId, AttemptStatus status) {
        return findByStudentId(studentId).stream().filter(attempt -> status == null || status.equals(attempt.getStatus())).toList();
    }

    @Override
    public List<QuizAttempt> findByStudentIdAndQuizId(UUID studentId, UUID quizId) {
        return findByStudentId(studentId).stream().filter(attempt -> attempt.getQuiz() != null && quizId.equals(attempt.getQuiz().getId())).toList();
    }

    @Override
    public QuizAttempt update(UUID id, QuizAttempt attempt) {
        QuizAttempt existing = findById(id).orElseThrow(() -> new RuntimeException("QuizAttempt not found"));
        if (attempt.getStatus() != null) existing.setStatus(attempt.getStatus());
        if (attempt.getScore() != null) existing.setScore(attempt.getScore());
        if (attempt.getTotalQuestions() != null) existing.setTotalQuestions(attempt.getTotalQuestions());
        if (attempt.getCorrectAnswers() != null) existing.setCorrectAnswers(attempt.getCorrectAnswers());
        if (attempt.getStatus() == AttemptStatus.COMPLETED) existing.setSubmittedAt(Instant.now());
        return quizAttemptRepository.save(existing);
    }

    @Override
    public void delete(UUID id) {
        findById(id).ifPresent(quizAttemptRepository::delete);
    }
}
