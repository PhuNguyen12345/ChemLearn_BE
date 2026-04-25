package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.request.QuestionRequest;
import com.example.chemlearn.lms.dto.response.QuestionResponse;
import com.example.chemlearn.lms.entity.Question;
import com.example.chemlearn.lms.enums.QuestionType;
import com.example.chemlearn.lms.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lms/questions")
@CrossOrigin(origins = "*")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @PostMapping
    public ResponseEntity<QuestionResponse> create(@RequestBody QuestionRequest request) {
        Question question = new Question();
        question.setContent(request.getContent());
        question.setQuestionType(request.getQuestionType() != null ? request.getQuestionType() : QuestionType.SINGLE_CHOICE);
        question.setExplanation(request.getExplanation());
        question.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0);
        Question created = questionService.create(question);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponse> findById(@PathVariable UUID id) {
        return questionService.findById(id)
                .map(question -> ResponseEntity.ok(mapToResponse(question)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<QuestionResponse>> findAll() {
        List<QuestionResponse> questions = questionService.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<QuestionResponse>> findByLessonId(@PathVariable UUID lessonId) {
        List<QuestionResponse> questions = questionService.findByLessonId(lessonId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<List<QuestionResponse>> findByQuizId(@PathVariable UUID quizId) {
        List<QuestionResponse> questions = questionService.findByQuizId(quizId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/type/{questionType}")
    public ResponseEntity<List<QuestionResponse>> findByQuestionType(@PathVariable QuestionType questionType) {
        List<QuestionResponse> questions = questionService.findByQuestionType(questionType)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(questions);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionResponse> update(@PathVariable UUID id, @RequestBody QuestionRequest request) {
        Question question = new Question();
        question.setContent(request.getContent());
        question.setQuestionType(request.getQuestionType());
        question.setExplanation(request.getExplanation());
        question.setOrderIndex(request.getOrderIndex());
        Question updated = questionService.update(id, question);
        return ResponseEntity.ok(mapToResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        questionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private QuestionResponse mapToResponse(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getLesson() != null ? question.getLesson().getId() : null,
                question.getQuiz() != null ? question.getQuiz().getId() : null,
                question.getContent(),
                question.getQuestionType(),
                question.getExplanation(),
                question.getOrderIndex()
        );
    }
}
