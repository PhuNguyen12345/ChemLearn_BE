package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.request.QuizAttemptRequest;
import com.example.chemlearn.lms.dto.response.QuizAttemptResponse;
import com.example.chemlearn.lms.entity.QuizAttempt;
import com.example.chemlearn.lms.enums.AttemptStatus;
import com.example.chemlearn.lms.service.QuizAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lms/quiz-attempts")
@CrossOrigin(origins = "*")
public class QuizAttemptController {

    @Autowired
    private QuizAttemptService quizAttemptService;

    @PostMapping
    public ResponseEntity<QuizAttemptResponse> create(@RequestBody QuizAttemptRequest request) {
        QuizAttempt attempt = new QuizAttempt();
        attempt.setStatus(request.getStatus() != null ? request.getStatus() : AttemptStatus.IN_PROGRESS);
        attempt.setScore(request.getScore());
        attempt.setTotalQuestions(request.getTotalQuestions() != null ? request.getTotalQuestions() : 0);
        attempt.setCorrectAnswers(request.getCorrectAnswers() != null ? request.getCorrectAnswers() : 0);
        QuizAttempt created = quizAttemptService.create(attempt);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizAttemptResponse> findById(@PathVariable UUID id) {
        return quizAttemptService.findById(id)
                .map(attempt -> ResponseEntity.ok(mapToResponse(attempt)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<QuizAttemptResponse>> findAll() {
        List<QuizAttemptResponse> attempts = quizAttemptService.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<QuizAttemptResponse>> findByStudentId(@PathVariable UUID studentId) {
        List<QuizAttemptResponse> attempts = quizAttemptService.findByStudentId(studentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<List<QuizAttemptResponse>> findByQuizId(@PathVariable UUID quizId) {
        List<QuizAttemptResponse> attempts = quizAttemptService.findByQuizId(quizId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<QuizAttemptResponse>> findByLessonId(@PathVariable UUID lessonId) {
        List<QuizAttemptResponse> attempts = quizAttemptService.findByLessonId(lessonId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/student/{studentId}/status/{status}")
    public ResponseEntity<List<QuizAttemptResponse>> findByStudentIdAndStatus(
            @PathVariable UUID studentId, @PathVariable AttemptStatus status) {
        List<QuizAttemptResponse> attempts = quizAttemptService.findByStudentIdAndStatus(studentId, status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(attempts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizAttemptResponse> update(@PathVariable UUID id, @RequestBody QuizAttemptRequest request) {
        QuizAttempt attempt = new QuizAttempt();
        attempt.setStatus(request.getStatus());
        attempt.setScore(request.getScore());
        attempt.setTotalQuestions(request.getTotalQuestions());
        attempt.setCorrectAnswers(request.getCorrectAnswers());
        QuizAttempt updated = quizAttemptService.update(id, attempt);
        return ResponseEntity.ok(mapToResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        quizAttemptService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private QuizAttemptResponse mapToResponse(QuizAttempt attempt) {
        return new QuizAttemptResponse(
                attempt.getId(),
                attempt.getStudent() != null ? attempt.getStudent().getId() : null,
                attempt.getQuiz() != null ? attempt.getQuiz().getId() : null,
                attempt.getLesson() != null ? attempt.getLesson().getId() : null,
                attempt.getStatus(),
                attempt.getScore(),
                attempt.getTotalQuestions(),
                attempt.getCorrectAnswers(),
                attempt.getStartedAt(),
                attempt.getSubmittedAt()
        );
    }
}
