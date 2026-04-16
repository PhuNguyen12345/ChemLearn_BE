package com.example.chemlearn.controller;

import com.example.chemlearn.dtos.quiz.*;
import com.example.chemlearn.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/free")
    public List<QuizListItemDTO> getFreeQuizzes() {
        return quizService.getFreeQuizzes();
    }

    @GetMapping("/{quizId}")
    public QuizDetailDTO getQuiz(@PathVariable Long quizId) {
        return quizService.getQuizDetail(quizId);
    }

    @PostMapping("/{quizId}/attempts")
    public StartQuizAttemptResponseDTO startAttempt(
            @PathVariable Long quizId,
            Authentication authentication
    ) {
        return quizService.startAttempt(quizId, authentication.getName());
    }

    @PostMapping("/attempts/{attemptId}/submit")
    public QuizSubmitResponseDTO submitAttempt(
            @PathVariable Long attemptId,
            @Valid @RequestBody QuizSubmitRequestDTO requestDTO,
            Authentication authentication
    ) {
        return quizService.submitAttempt(attemptId, requestDTO, authentication.getName());
    }
}
