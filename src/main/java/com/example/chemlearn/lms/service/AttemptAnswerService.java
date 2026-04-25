package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.entity.AttemptAnswer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttemptAnswerService {
    AttemptAnswer create(AttemptAnswer attemptAnswer);
    Optional<AttemptAnswer> findById(UUID id);
    List<AttemptAnswer> findAll();
    List<AttemptAnswer> findByAttemptId(UUID attemptId);
    List<AttemptAnswer> findByAttemptIdAndIsCorrect(UUID attemptId, Boolean isCorrect);
    List<AttemptAnswer> findByQuestionId(UUID questionId);
    AttemptAnswer update(UUID id, AttemptAnswer attemptAnswer);
    void delete(UUID id);
}
