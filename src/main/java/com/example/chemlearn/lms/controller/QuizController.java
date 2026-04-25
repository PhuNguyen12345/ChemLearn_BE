package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.quiz.*;
import com.example.chemlearn.lms.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    public QuizDetailDTO getQuiz(@PathVariable UUID quizId) {
        return quizService.getQuizDetail(quizId);
    }

    @PostMapping("/{quizId}/attempts")
    public StartQuizAttemptResponseDTO startAttempt(
            @PathVariable UUID quizId,
            Authentication authentication
    ) {
        return quizService.startAttempt(quizId, authentication.getName());
    }

    @PostMapping("/attempts/{attemptId}/submit")
    public QuizSubmitResponseDTO submitAttempt(
            @PathVariable UUID attemptId,
            @Valid @RequestBody QuizSubmitRequestDTO requestDTO,
            Authentication authentication
    ) {
        return quizService.submitAttempt(attemptId, requestDTO, authentication.getName());
    }
}

