package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.lms.entity.AttemptAnswer;
import com.example.chemlearn.lms.repository.AttemptAnswerRepository;
import com.example.chemlearn.lms.service.AttemptAnswerService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AttemptAnswerServiceImpl implements AttemptAnswerService {
    @Autowired
    private AttemptAnswerRepository attemptAnswerRepository;

    @Override
    public AttemptAnswer create(AttemptAnswer attemptAnswer) {
        return attemptAnswerRepository.save(attemptAnswer);
    }

    @Override
    public Optional<AttemptAnswer> findById(UUID id) {
        return attemptAnswerRepository.findAll().stream()
                .filter(answer -> id.equals(answer.getId()))
                .findFirst();
    }

    @Override
    public List<AttemptAnswer> findAll() {
        return attemptAnswerRepository.findAll();
    }

    @Override
    public List<AttemptAnswer> findByAttemptId(UUID attemptId) {
        return attemptAnswerRepository.findAll().stream()
                .filter(answer -> answer.getAttempt() != null && attemptId.equals(answer.getAttempt().getId()))
                .toList();
    }

    @Override
    public List<AttemptAnswer> findByAttemptIdAndIsCorrect(UUID attemptId, Boolean isCorrect) {
        return findByAttemptId(attemptId).stream()
                .filter(answer -> isCorrect == null || isCorrect.equals(answer.getIsCorrect()))
                .toList();
    }

    @Override
    public List<AttemptAnswer> findByQuizQuestionId(UUID questionId) {
        return attemptAnswerRepository.findAll().stream()
                .filter(answer -> answer.getQuizQuestion() != null && questionId.equals(answer.getQuizQuestion().getId()))
                .toList();
    }

    @Override
    public AttemptAnswer update(UUID id, AttemptAnswer attemptAnswer) {
        AttemptAnswer existing = findById(id)
                .orElseThrow(() -> new RuntimeException("AttemptAnswer not found"));
        if (attemptAnswer.getIsCorrect() != null) existing.setIsCorrect(attemptAnswer.getIsCorrect());
        if (attemptAnswer.getSelectedOption() != null) existing.setSelectedOption(attemptAnswer.getSelectedOption());
        if (attemptAnswer.getQuizQuestion() != null) existing.setQuizQuestion(attemptAnswer.getQuizQuestion());
        return attemptAnswerRepository.save(existing);
    }

    @Override
    public void delete(UUID id) {
        findById(id).ifPresent(attemptAnswerRepository::delete);
    }
}
