package com.example.chemlearn.service;

import com.example.chemlearn.dtos.quiz.*;

import java.util.List;

public interface QuizService {
    List<QuizListItemDTO> getFreeQuizzes();

    QuizDetailDTO getQuizDetail(Long quizId);

    StartQuizAttemptResponseDTO startAttempt(Long quizId, String username);

    QuizSubmitResponseDTO submitAttempt(Long attemptId, QuizSubmitRequestDTO requestDTO, String username);
}
