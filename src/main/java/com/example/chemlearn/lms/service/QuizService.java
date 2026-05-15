package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.dto.quiz.*;

import java.util.List;
import java.util.UUID;

public interface QuizService {
    List<QuizListItemDTO> getFreeQuizzes(String studentUsername);

    QuizDetailDTO getQuizDetail(UUID quizId, String studentUsername);

    StartQuizAttemptResponseDTO startAttempt(UUID quizId, String username);

    QuizSubmitResponseDTO submitAttempt(UUID attemptId, QuizSubmitRequestDTO requestDTO, String username);

    List<QuizAttemptHistoryDTO> getAttemptHistory(UUID quizId, String username);
}

