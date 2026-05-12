package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.request.AttemptAnswerRequest;
import com.example.chemlearn.lms.dto.response.AttemptAnswerResponse;
import com.example.chemlearn.lms.entity.AttemptAnswer;
import com.example.chemlearn.lms.service.impl.AttemptAnswerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lms/attempt-answers")
@CrossOrigin(origins = "*")
public class AttemptAnswerController {

    @Autowired
    private AttemptAnswerServiceImpl attemptAnswerService;

    @PostMapping
    public ResponseEntity<AttemptAnswerResponse> create(@RequestBody AttemptAnswerRequest request) {
        AttemptAnswer attemptAnswer = new AttemptAnswer();
        AttemptAnswer created = attemptAnswerService.create(attemptAnswer);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttemptAnswerResponse> findById(@PathVariable UUID id) {
        return attemptAnswerService.findById(id)
                .map(attemptAnswer -> ResponseEntity.ok(mapToResponse(attemptAnswer)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<AttemptAnswerResponse>> findAll() {
        List<AttemptAnswerResponse> answers = attemptAnswerService.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(answers);
    }

    @GetMapping("/attempt/{attemptId}")
    public ResponseEntity<List<AttemptAnswerResponse>> findByAttemptId(@PathVariable UUID attemptId) {
        List<AttemptAnswerResponse> answers = attemptAnswerService.findByAttemptId(attemptId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(answers);
    }

    @GetMapping("/attempt/{attemptId}/correct")
    public ResponseEntity<List<AttemptAnswerResponse>> findByAttemptIdAndIsCorrect(@PathVariable UUID attemptId) {
        List<AttemptAnswerResponse> answers = attemptAnswerService.findByAttemptIdAndIsCorrect(attemptId, true)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(answers);
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<AttemptAnswerResponse>> findByQuestionId(@PathVariable UUID questionId) {
        List<AttemptAnswerResponse> answers = attemptAnswerService.findByQuizQuestionId(questionId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(answers);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttemptAnswerResponse> update(@PathVariable UUID id, @RequestBody AttemptAnswerRequest request) {
        AttemptAnswer attemptAnswer = new AttemptAnswer();
        AttemptAnswer updated = attemptAnswerService.update(id, attemptAnswer);
        return ResponseEntity.ok(mapToResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        attemptAnswerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private AttemptAnswerResponse mapToResponse(AttemptAnswer attemptAnswer) {
        return new AttemptAnswerResponse(
                attemptAnswer.getId(),
                attemptAnswer.getAttempt() != null ? attemptAnswer.getAttempt().getId() : null,
                attemptAnswer.getQuizQuestion() != null ? attemptAnswer.getQuizQuestion().getId() : null,
                attemptAnswer.getSelectedOption() != null ? attemptAnswer.getSelectedOption() : null,
                attemptAnswer.getIsCorrect()
        );
    }
}
