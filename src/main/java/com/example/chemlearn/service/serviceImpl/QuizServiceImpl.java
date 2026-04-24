package com.example.chemlearn.service.serviceImpl;

import com.example.chemlearn.dtos.quiz.*;
import com.example.chemlearn.entity.*;
import com.example.chemlearn.enums.AttemptStatus;
import com.example.chemlearn.enums.QuizType;
import com.example.chemlearn.exception.CustomExceptions;
import com.example.chemlearn.repository.*;
import com.example.chemlearn.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final AttemptAnswerRepository attemptAnswerRepository;
    private final AccountRepository accountRepository;

    @Override
    public List<QuizListItemDTO> getFreeQuizzes() {
        List<Quiz> quizzes = quizRepository.findByPublishedTrueAndQuizTypeOrderByIdAsc(QuizType.FREE);
        return quizzes.stream().map(quiz -> {
            int questionCount = quizQuestionRepository.findByQuizIdOrderByDisplayOrderAsc(quiz.getId()).size();
            return new QuizListItemDTO(
                    quiz.getId(),
                    quiz.getTitle(),
                    quiz.getDescription(),
                    quiz.getQuizType(),
                    quiz.getDurationMinutes(),
                    questionCount
            );
        }).toList();
    }

    @Override
    public QuizDetailDTO getQuizDetail(Long quizId) {
        Quiz quiz = quizRepository.findByIdAndPublishedTrue(quizId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Quiz not found"));

        List<QuizQuestionDTO> questions = quizQuestionRepository
                .findByQuizIdOrderByDisplayOrderAsc(quiz.getId())
                .stream()
                .map(q -> new QuizQuestionDTO(
                        q.getId(),
                        q.getPrompt(),
                        q.getOptionA(),
                        q.getOptionB(),
                        q.getOptionC(),
                        q.getOptionD(),
                        q.getDisplayOrder()
                ))
                .toList();

        return new QuizDetailDTO(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getQuizType(),
                quiz.getDurationMinutes(),
                questions
        );
    }

    @Override
    @Transactional
    public StartQuizAttemptResponseDTO startAttempt(Long quizId, String username) {
        Quiz quiz = quizRepository.findByIdAndPublishedTrue(quizId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Quiz not found"));

        Account student = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Student account not found"));

        QuizAttempt activeAttempt = quizAttemptRepository
            .findFirstByQuizIdAndStudentIdAndStatusOrderByStartedAtDesc(quizId, student.getId(), AttemptStatus.IN_PROGRESS)
            .orElse(null);

        if (activeAttempt != null) {
            return new StartQuizAttemptResponseDTO(
                activeAttempt.getId(),
                quiz.getId(),
                activeAttempt.getStatus(),
                activeAttempt.getStartedAt()
            );
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setStudent(student);
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        attempt.setStartedAt(LocalDateTime.now());

        quizAttemptRepository.save(attempt);
        return new StartQuizAttemptResponseDTO(
                attempt.getId(),
                quiz.getId(),
                attempt.getStatus(),
                attempt.getStartedAt()
        );
    }

    @Override
    @Transactional
    public QuizSubmitResponseDTO submitAttempt(Long attemptId, QuizSubmitRequestDTO requestDTO, String username) {
        Account student = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Student account not found"));

        QuizAttempt attempt = quizAttemptRepository.findByIdAndStudentId(attemptId, student.getId())
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Attempt not found"));

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new CustomExceptions.BadRequestException("Attempt is already submitted");
        }

        List<QuizQuestion> questions = quizQuestionRepository.findByQuizIdOrderByDisplayOrderAsc(attempt.getQuiz().getId());
        if (questions.isEmpty()) {
            throw new CustomExceptions.BadRequestException("Quiz has no questions");
        }

        Map<Long, String> answerMap = requestDTO.getAnswers()
                .stream()
                .collect(Collectors.toMap(QuizAnswerDTO::getQuestionId, dto -> dto.getSelectedOption().toUpperCase(), (a, b) -> b));

        // Validation for illegal combinations: all submitted question IDs must belong to this quiz.
        Set<Long> validQuestionIds = questions.stream().map(QuizQuestion::getId).collect(Collectors.toSet());
        for (Long submittedQuestionId : answerMap.keySet()) {
            if (!validQuestionIds.contains(submittedQuestionId)) {
                throw new CustomExceptions.BadRequestException("Submitted question does not belong to the quiz");
            }
        }

        int total = questions.size();
        int correct = 0;

        List<AttemptAnswer> attemptAnswers = new ArrayList<>();
        for (QuizQuestion question : questions) {
            String selectedOption = answerMap.get(question.getId());
            if (selectedOption == null) {
                continue;
            }

            boolean isCorrect = selectedOption.equalsIgnoreCase(question.getCorrectOption());
            if (isCorrect) {
                correct++;
            }

            AttemptAnswer answer = new AttemptAnswer();
            answer.setAttempt(attempt);
            answer.setQuestion(question);
            answer.setSelectedOption(selectedOption);
            answer.setCorrect(isCorrect);
            attemptAnswers.add(answer);
        }

        if (!attemptAnswers.isEmpty()) {
            attemptAnswerRepository.saveAll(attemptAnswers);
        }

        int score = Math.round((correct * 100.0f) / total);
        attempt.setCorrectAnswers(correct);
        attempt.setTotalQuestions(total);
        attempt.setScore(score);
        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt.setSubmittedAt(LocalDateTime.now());

        quizAttemptRepository.save(attempt);

        return new QuizSubmitResponseDTO(
                attempt.getId(),
                total,
                correct,
                score,
                attempt.getStatus(),
                attempt.getSubmittedAt()
        );
    }
}
