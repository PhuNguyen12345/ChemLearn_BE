package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.lms.entity.Answer;
import com.example.chemlearn.lms.repository.AnswerRepository;
import com.example.chemlearn.lms.service.AnswerService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnswerServiceImpl implements AnswerService {
    @Autowired
    private AnswerRepository answerRepository;

    @Override
    public Answer create(Answer answer) {
        return answerRepository.save(answer);
    }

    @Override
    public Optional<Answer> findById(UUID id) {
        return answerRepository.findById(id);
    }

    @Override
    public List<Answer> findAll() {
        return answerRepository.findAll();
    }

    @Override
    public List<Answer> findByQuestionId(UUID questionId) {
        return answerRepository.findByQuestionIdOrderByOrderIndex(questionId);
    }

    @Override
    public List<Answer> findCorrectAnswers(UUID questionId) {
        return answerRepository.findByQuestionIdAndIsCorrect(questionId, true);
    }

    @Override
    public Answer update(UUID id, Answer answer) {
        Answer existing = answerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
        if (answer.getContent() != null) existing.setContent(answer.getContent());
        if (answer.getIsCorrect() != null) existing.setIsCorrect(answer.getIsCorrect());
        if (answer.getOrderIndex() != null) existing.setOrderIndex(answer.getOrderIndex());
        return answerRepository.save(existing);
    }

    @Override
    public void delete(UUID id) {
        answerRepository.deleteById(id);
    }
}
