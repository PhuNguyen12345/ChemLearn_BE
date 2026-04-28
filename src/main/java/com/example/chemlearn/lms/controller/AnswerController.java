package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.request.AnswerRequest;
import com.example.chemlearn.lms.dto.response.AnswerResponse;
import com.example.chemlearn.lms.entity.Answer;
import com.example.chemlearn.lms.service.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lms/answers")
@CrossOrigin(origins = "*")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    @PostMapping
    public ResponseEntity<AnswerResponse> create(@RequestBody AnswerRequest request) {
        Answer answer = new Answer();
        answer.setContent(request.getContent());
        answer.setIsCorrect(request.getIsCorrect() != null ? request.getIsCorrect() : false);
        answer.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0);
        Answer created = answerService.create(answer);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnswerResponse> findById(@PathVariable UUID id) {
        return answerService.findById(id)
                .map(answer -> ResponseEntity.ok(mapToResponse(answer)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<AnswerResponse>> findAll() {
        List<AnswerResponse> answers = answerService.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(answers);
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<AnswerResponse>> findByQuestionId(@PathVariable UUID questionId) {
        List<AnswerResponse> answers = answerService.findByQuestionId(questionId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(answers);
    }

    @GetMapping("/question/{questionId}/correct")
    public ResponseEntity<List<AnswerResponse>> findCorrectAnswers(@PathVariable UUID questionId) {
        List<AnswerResponse> answers = answerService.findCorrectAnswers(questionId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(answers);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnswerResponse> update(@PathVariable UUID id, @RequestBody AnswerRequest request) {
        Answer answer = new Answer();
        answer.setContent(request.getContent());
        answer.setIsCorrect(request.getIsCorrect());
        answer.setOrderIndex(request.getOrderIndex());
        Answer updated = answerService.update(id, answer);
        return ResponseEntity.ok(mapToResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        answerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private AnswerResponse mapToResponse(Answer answer) {
        return new AnswerResponse(
                answer.getId(),
                answer.getQuestion() != null ? answer.getQuestion().getId() : null,
                answer.getContent(),
                answer.getIsCorrect(),
                answer.getOrderIndex()
        );
    }
}
