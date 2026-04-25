package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.lms.entity.Question;
import com.example.chemlearn.lms.enums.QuestionType;
import com.example.chemlearn.lms.repository.QuestionRepository;
import com.example.chemlearn.lms.service.QuestionService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuestionServiceImpl implements QuestionService {
    @Autowired
    private QuestionRepository questionRepository;

    @Override
    public Question create(Question question) {
        return questionRepository.save(question);
    }

    @Override
    public Optional<Question> findById(UUID id) {
        return questionRepository.findAll().stream().filter(question -> id.equals(question.getId())).findFirst();
    }

    @Override
    public List<Question> findAll() {
        return questionRepository.findAll();
    }

    @Override
    public List<Question> findByLessonId(UUID lessonId) {
        return questionRepository.findByLessonIdOrderByOrderIndex(lessonId);
    }

    @Override
    public List<Question> findByQuizId(UUID quizId) {
        return questionRepository.findByQuizIdOrderByOrderIndex(quizId);
    }

    @Override
    public List<Question> findByQuestionType(QuestionType questionType) {
        return questionRepository.findByQuestionType(questionType);
    }

    @Override
    public Question update(UUID id, Question question) {
        Question existing = findById(id).orElseThrow(() -> new RuntimeException("Question not found"));
        if (question.getLesson() != null) existing.setLesson(question.getLesson());
        if (question.getQuiz() != null) existing.setQuiz(question.getQuiz());
        if (question.getContent() != null) existing.setContent(question.getContent());
        if (question.getQuestionType() != null) existing.setQuestionType(question.getQuestionType());
        if (question.getExplanation() != null) existing.setExplanation(question.getExplanation());
        if (question.getOrderIndex() != null) existing.setOrderIndex(question.getOrderIndex());
        return questionRepository.save(existing);
    }

    @Override
    public void delete(UUID id) {
        findById(id).ifPresent(questionRepository::delete);
    }
}
