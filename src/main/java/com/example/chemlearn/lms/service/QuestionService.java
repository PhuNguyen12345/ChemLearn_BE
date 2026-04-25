package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.entity.Question;
import com.example.chemlearn.lms.enums.QuestionType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionService {
    Question create(Question question);
    Optional<Question> findById(UUID id);
    List<Question> findAll();
    List<Question> findByLessonId(UUID lessonId);
    List<Question> findByQuizId(UUID quizId);
    List<Question> findByQuestionType(QuestionType questionType);
    Question update(UUID id, Question question);
    void delete(UUID id);
}
