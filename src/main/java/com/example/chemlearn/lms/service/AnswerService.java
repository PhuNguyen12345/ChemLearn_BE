package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.entity.Answer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnswerService {
    Answer create(Answer answer);
    Optional<Answer> findById(UUID id);
    List<Answer> findAll();
    List<Answer> findByQuestionId(UUID questionId);
    List<Answer> findCorrectAnswers(UUID questionId);
    Answer update(UUID id, Answer answer);
    void delete(UUID id);
}
