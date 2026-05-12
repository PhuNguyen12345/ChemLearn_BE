package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.lms.dto.quiz.QuizAnswerDTO;
import com.example.chemlearn.lms.dto.quiz.QuizDetailDTO;
import com.example.chemlearn.lms.dto.quiz.QuizListItemDTO;
import com.example.chemlearn.lms.dto.quiz.QuizQuestionDTO;
import com.example.chemlearn.lms.dto.quiz.QuizSubmitRequestDTO;
import com.example.chemlearn.lms.dto.quiz.QuizSubmitResponseDTO;
import com.example.chemlearn.lms.dto.quiz.StartQuizAttemptResponseDTO;
import com.example.chemlearn.lms.entity.*;
import com.example.chemlearn.lms.enums.AttemptStatus;
import com.example.chemlearn.lms.enums.QuizType;
import com.example.chemlearn.lms.exception.CustomExceptions;
import com.example.chemlearn.lms.repository.ClassStudentLinkRepository;
import com.example.chemlearn.lms.repository.UserRepository;
import com.example.chemlearn.lms.repository.AttemptAnswerRepository;
import com.example.chemlearn.lms.repository.QuizAttemptRepository;
import com.example.chemlearn.lms.repository.QuizQuestionRepository;
import com.example.chemlearn.lms.repository.QuizRepository;
import com.example.chemlearn.lms.repository.StudyClassAssignmentRepository;
import com.example.chemlearn.lms.service.QuizService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final AttemptAnswerRepository attemptAnswerRepository;
    private final UserRepository userRepository;
        private final ClassStudentLinkRepository classStudentLinkRepository;
        private final StudyClassAssignmentRepository assignmentRepository;

    @Override
        public List<QuizListItemDTO> getFreeQuizzes(String studentUsername) {
            requireStudentUser(studentUsername);
            return quizRepository.findByPublishedTrueAndQuizTypeOrderByIdAsc(QuizType.FREE).stream()
                .map(quiz -> new QuizListItemDTO(
                        quiz.getId(),
                        quiz.getTitle(),
                        quiz.getDescription(),
                        quiz.getQuizType(),
                        quiz.getDurationMinutes(),
                        Math.toIntExact(quizQuestionRepository.countByQuizId(quiz.getId()))))
                .toList();
    }

    @Override
    public QuizDetailDTO getQuizDetail(UUID quizId, String studentUsername) {
        User studentUser = requireStudentUser(studentUsername);
        Quiz quiz = requireVisibleQuiz(quizId, studentUser.getId());
        List<QuizQuestionDTO> questions = quizQuestionRepository.findByQuizIdOrderByDisplayOrderAsc(quizId).stream()
                .map(question -> new QuizQuestionDTO(
                        question.getId(),
                        question.getQuestionType(),
                        question.getPrompt(),
                        question.getOptionA(),
                        question.getOptionB(),
                        question.getOptionC(),
                        question.getOptionD(),
                        question.getDisplayOrder()))
                .toList();
        return new QuizDetailDTO(
                quizId,
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getQuizType(),
                quiz.getDurationMinutes(),
                questions);
    }

    @Override
    @Transactional
    public StartQuizAttemptResponseDTO startAttempt(UUID quizId, String username) {
        User studentAccount = requireStudentUser(username);
        Quiz quiz = requireVisibleQuiz(quizId, studentAccount.getId());

        if (isExamQuiz(quiz) && hasCompletedAttempt(quizId, studentAccount.getId())) {
            throw new CustomExceptions.BadRequestException("This exam can only be submitted once");
        }

        if (isAssignmentQuiz(quiz)) {
            Instant dueAt = resolveStudentQuizDueAt(quizId, studentAccount.getId());
            if (dueAt != null && Instant.now().isAfter(dueAt)) {
                throw new CustomExceptions.BadRequestException("This assignment is past the deadline");
            }
        }

        QuizAttempt activeAttempt = quizAttemptRepository
                .findFirstByQuizIdAndStudentIdAndStatusOrderByStartedAtDesc(quizId, studentAccount.getId(), AttemptStatus.IN_PROGRESS)
                .orElse(null);
        if (activeAttempt != null) {
            return new StartQuizAttemptResponseDTO(
                    activeAttempt.getId(),
                    quizId,
                    activeAttempt.getStatus(),
                    activeAttempt.getStartedAt());
        }

        Student student = new Student();
        student.setId(studentAccount.getId());

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setStudent(student);
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        attempt.setScore(BigDecimal.ZERO);
        attempt.setTotalQuestions(0);
        attempt.setCorrectAnswers(0);
        attempt.setStartedAt(Instant.now());
        quizAttemptRepository.save(attempt);

        return new StartQuizAttemptResponseDTO(
                attempt.getId(),
                quizId,
                attempt.getStatus(),
                attempt.getStartedAt());
    }

    @Override
    @Transactional
    public QuizSubmitResponseDTO submitAttempt(UUID attemptId, QuizSubmitRequestDTO requestDTO, String username) {
        User studentAccount = requireStudentUser(username);
        QuizAttempt attempt = quizAttemptRepository.findByIdAndStudentId(attemptId, studentAccount.getId())
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Attempt not found"));
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new CustomExceptions.BadRequestException("Attempt is already submitted");
        }

        Quiz quiz = attempt.getQuiz();
        if (quiz != null && isAssignmentQuiz(quiz)) {
            Instant dueAt = resolveStudentQuizDueAt(quiz.getId(), studentAccount.getId());
            if (dueAt != null && Instant.now().isAfter(dueAt)) {
                throw new CustomExceptions.BadRequestException("This assignment is past the deadline");
            }
        }

        List<QuizQuestion> quizQuestions = quizQuestionRepository.findByQuizIdOrderByDisplayOrderAsc(attempt.getQuiz().getId());
        Map<UUID, String> correctOptions = quizQuestions.stream()
                .collect(Collectors.toMap(QuizQuestion::getId, question -> question.getCorrectOption().toUpperCase(), (left, right) -> right, HashMap::new));

        Map<UUID, com.example.chemlearn.lms.enums.QuestionType> questionTypes = quizQuestions.stream()
                .collect(Collectors.toMap(QuizQuestion::getId, QuizQuestion::getQuestionType, (left, right) -> right, HashMap::new));

        int total = quizQuestions.size();
        int correct = 0;
        boolean hasEssay = false;

        for (QuizAnswerDTO answer : requestDTO.getAnswers()) {
            QuizQuestion quizQuestion = quizQuestions.stream()
                .filter(q -> q.getId().equals(answer.getQuestionId()))
                .findFirst()
                .orElse(null);
            
            if (quizQuestion == null) continue;

            AttemptAnswer attemptAnswer = new AttemptAnswer();
            attemptAnswer.setAttempt(attempt);
            attemptAnswer.setQuizQuestion(quizQuestion);
            attemptAnswer.setSelectedOption(answer.getSelectedOption());
            
            com.example.chemlearn.lms.enums.QuestionType type = quizQuestion.getQuestionType();
            if (type == com.example.chemlearn.lms.enums.QuestionType.ESSAY) {
                hasEssay = true;
                attemptAnswer.setIsCorrect(false);
            } else {
                String expected = quizQuestion.getCorrectOption();
                boolean isAnswerCorrect = false;
                if (expected != null && answer.getSelectedOption() != null) {
                    if (type == com.example.chemlearn.lms.enums.QuestionType.MULTIPLE_CHOICE) {
                        String[] expectedParts = expected.toUpperCase().split(",");
                        String[] answerParts = answer.getSelectedOption().toUpperCase().split(",");
                        java.util.Arrays.sort(expectedParts);
                        java.util.Arrays.sort(answerParts);
                        if (java.util.Arrays.equals(expectedParts, answerParts)) {
                            isAnswerCorrect = true;
                        }
                    } else if (expected.equalsIgnoreCase(answer.getSelectedOption())) {
                        isAnswerCorrect = true;
                    }
                }
                attemptAnswer.setIsCorrect(isAnswerCorrect);
                if (isAnswerCorrect) {
                    correct++;
                }
            }
            attemptAnswerRepository.save(attemptAnswer);
        }

        attempt.setCorrectAnswers(correct);
        attempt.setTotalQuestions(total);
        attempt.setScore(total == 0 ? BigDecimal.ZERO : BigDecimal.valueOf((correct * 100.0) / total));
        
        if (hasEssay && (attempt.getQuiz() == null || attempt.getQuiz().getQuizType() != QuizType.FREE)) {
            attempt.setStatus(AttemptStatus.NEEDS_GRADING);
        } else {
            attempt.setStatus(AttemptStatus.COMPLETED);
        }
        
        attempt.setSubmittedAt(Instant.now());
        quizAttemptRepository.save(attempt);

        return new QuizSubmitResponseDTO(
                attempt.getId(),
                total,
                correct,
                attempt.getScore() == null ? 0 : attempt.getScore().intValue(),
                attempt.getStatus(),
                                attempt.getSubmittedAt());
    }

    private User requireStudentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Student account not found"));
        if (user.getRole() != UserRole.ROLE_STUDENT) {
            throw new CustomExceptions.BadRequestException("Current account is not a student");
        }
        return user;
    }

    private Quiz requireVisibleQuiz(UUID quizId, UUID studentId) {
        Quiz quiz = quizRepository.findByIdAndPublishedTrue(quizId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Quiz not found"));
        if (quiz.getQuizType() == QuizType.FREE) {
            return quiz;
        }
        if (!isVisibleToStudent(quizId, studentId)) {
            throw new CustomExceptions.ResourceNotFoundException("Quiz not found");
        }
        return quiz;
    }

    private boolean isVisibleToStudent(UUID quizId, UUID studentId) {
        return resolveStudentQuizAssignment(quizId, studentId).isPresent();
    }

    private boolean isExamQuiz(Quiz quiz) {
        return quiz.getQuizType() == QuizType.EXAM;
    }

    private boolean isAssignmentQuiz(Quiz quiz) {
        return quiz.getQuizType() == QuizType.ASSIGNMENT || quiz.getQuizType() == QuizType.MINI_QUIZ;
    }

    private boolean hasCompletedAttempt(UUID quizId, UUID studentId) {
        return quizAttemptRepository
                .findFirstByQuizIdAndStudentIdAndStatusOrderByStartedAtDesc(quizId, studentId, AttemptStatus.COMPLETED)
                .isPresent();
    }

    private Instant resolveStudentQuizDueAt(UUID quizId, UUID studentId) {
        return resolveStudentQuizAssignment(quizId, studentId)
                .map(StudyClassAssignment::getDueDate)
                .orElse(null);
    }

    private java.util.Optional<StudyClassAssignment> resolveStudentQuizAssignment(UUID quizId, UUID studentId) {
        List<UUID> classIds = classStudentLinkRepository.findByStudentId(studentId)
                .stream()
                .map(ClassStudentLink::getClassRoom)
                .filter(Objects::nonNull)
                .map(classRoom -> classRoom.getId())
                .toList();
        if (classIds.isEmpty()) {
            return java.util.Optional.empty();
        }

        return assignmentRepository.findByStudyClassField_IdIn(classIds)
                .stream()
            .filter(assignment -> assignment.getQuiz() != null
                && quizId.equals(assignment.getQuiz().getId())
                && Boolean.TRUE.equals(assignment.getQuiz().getPublished()))
            .max(Comparator.comparing(StudyClassAssignment::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())));
    }
}
