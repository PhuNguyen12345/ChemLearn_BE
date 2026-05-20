package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.lms.dto.quiz.*;
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
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
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
                    Math.toIntExact(quizQuestionRepository.countByQuizId(quiz.getId())),
                    null))
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
                        normalizePointValue(question.getPointValue()),
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

        rejectIfDeadlineReached(quiz, studentAccount.getId());

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
        rejectIfDeadlineReached(quiz, studentAccount.getId());

        List<QuizQuestion> quizQuestions = quizQuestionRepository.findByQuizIdOrderByDisplayOrderAsc(attempt.getQuiz().getId());
        Map<UUID, QuizAnswerDTO> submittedAnswers = requestDTO.getAnswers().stream()
                .collect(Collectors.toMap(QuizAnswerDTO::getQuestionId, Function.identity(), (left, right) -> right, HashMap::new));

        int total = quizQuestions.size();
        int correct = 0;
        BigDecimal awardedPoints = BigDecimal.ZERO;
        BigDecimal totalPoints = quizQuestions.stream()
                .map(question -> normalizePointValue(question.getPointValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean hasEssay = quizQuestionRepository.existsByQuizIdAndQuestionType(attempt.getQuiz().getId(), com.example.chemlearn.lms.enums.QuestionType.ESSAY);

        for (QuizQuestion quizQuestion : quizQuestions) {
            QuizAnswerDTO answer = submittedAnswers.get(quizQuestion.getId());
            AttemptAnswer attemptAnswer = new AttemptAnswer();
            attemptAnswer.setAttempt(attempt);
            attemptAnswer.setQuizQuestion(quizQuestion);
            attemptAnswer.setSelectedOption(answer == null ? null : answer.getSelectedOption());
            
            com.example.chemlearn.lms.enums.QuestionType type = quizQuestion.getQuestionType();
            if (type == com.example.chemlearn.lms.enums.QuestionType.ESSAY) {
                attemptAnswer.setIsCorrect(false);
                attemptAnswer.setAwardedPoints(null);
            } else {
                boolean isAnswerCorrect = isObjectiveAnswerCorrect(quizQuestion, answer == null ? null : answer.getSelectedOption());
                attemptAnswer.setIsCorrect(isAnswerCorrect);
                BigDecimal questionPoints = isAnswerCorrect ? normalizePointValue(quizQuestion.getPointValue()) : BigDecimal.ZERO;
                attemptAnswer.setAwardedPoints(questionPoints);
                if (isAnswerCorrect) {
                    correct++;
                }
                awardedPoints = awardedPoints.add(questionPoints);
            }
            attemptAnswerRepository.save(attemptAnswer);
        }

        attempt.setCorrectAnswers(correct);
        attempt.setTotalQuestions(total);
        
        if (hasEssay) {
            attempt.setStatus(AttemptStatus.NEEDS_GRADING);
            attempt.setScore(null);
        } else {
            attempt.setStatus(AttemptStatus.COMPLETED);
            attempt.setScore(calculatePercentage(awardedPoints, totalPoints));
        }
        
        attempt.setSubmittedAt(Instant.now());
        quizAttemptRepository.save(attempt);

        String message;
        if (attempt.getStatus() == AttemptStatus.NEEDS_GRADING) {
            message = "Submission success! Please wait for your teacher to grade.";
        } else {
            message = "Submission success! Your grade is ready.";
        }

        return new QuizSubmitResponseDTO(
                attempt.getId(),
                total,
                correct,
                attempt.getScore() == null ? null : attempt.getScore().intValue(),
                attempt.getStatus(),
                attempt.getSubmittedAt(),
                message);
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

    @Override
    public List<QuizAttemptHistoryDTO> getAttemptHistory(UUID quizId, String username) {
        User user = requireStudentUser(username);
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Quiz not found"));

        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizIdAndStudentIdOrderByStartedAtDesc(quizId, user.getId());

        Instant deadline = resolveStudentQuizDeadline(quiz, user.getId());
        boolean pastDeadline = deadline != null && isNowAtOrAfter(deadline);
        boolean isExam = isExamQuiz(quiz);

        return attempts.stream()
                .map(attempt -> {
                    boolean canRetake;
                    if (isExam) {
                        // Exams can only be submitted once (logic from startAttempt)
                        canRetake = false;
                    } else if (pastDeadline) {
                        // Past deadline
                        canRetake = false;
                    } else {
                        // If it's in progress, they are already "retaking" or continuing
                        // If completed or needs grading, they can retake if deadline allows
                        canRetake = true;
                    }

                    return new QuizAttemptHistoryDTO(
                            attempt.getId(),
                            attempt.getStatus(),
                            attempt.getScore(),
                            attempt.getTotalQuestions(),
                            attempt.getCorrectAnswers(),
                            attempt.getStartedAt(),
                            attempt.getSubmittedAt(),
                            canRetake
                    );
                })
                .toList();
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

    private void rejectIfDeadlineReached(Quiz quiz, UUID studentId) {
        Instant deadline = resolveStudentQuizDeadline(quiz, studentId);
        if (deadline != null && isNowAtOrAfter(deadline)) {
            String label = isExamQuiz(quiz) ? "exam" : "assignment";
            throw new CustomExceptions.BadRequestException("This " + label + " deadline has passed");
        }
    }

    private boolean isNowAtOrAfter(Instant deadline) {
        return !Instant.now().isBefore(deadline);
    }

    private boolean isObjectiveAnswerCorrect(QuizQuestion quizQuestion, String selectedOption) {
        String expected = quizQuestion.getCorrectOption();
        if (expected == null || selectedOption == null) {
            return false;
        }

        if (quizQuestion.getQuestionType() == com.example.chemlearn.lms.enums.QuestionType.MULTIPLE_CHOICE) {
            String[] expectedParts = splitAndNormalizeOptions(expected);
            String[] answerParts = splitAndNormalizeOptions(selectedOption);
            Arrays.sort(expectedParts);
            Arrays.sort(answerParts);
            return Arrays.equals(expectedParts, answerParts);
        }

        return expected.equalsIgnoreCase(selectedOption);
    }

    private String[] splitAndNormalizeOptions(String options) {
        return Arrays.stream(options.toUpperCase().split(","))
                .map(String::trim)
                .filter(option -> !option.isBlank())
                .toArray(String[]::new);
    }

    private BigDecimal normalizePointValue(BigDecimal pointValue) {
        if (pointValue == null || pointValue.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ONE;
        }
        return pointValue;
    }

    private BigDecimal calculatePercentage(BigDecimal awardedPoints, BigDecimal totalPoints) {
        if (totalPoints == null || totalPoints.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return awardedPoints
                .multiply(BigDecimal.valueOf(100))
                .divide(totalPoints, 2, RoundingMode.HALF_UP);
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

    private Instant resolveStudentQuizDeadline(Quiz quiz, UUID studentId) {
        if (quiz == null) {
            return null;
        }
        Instant assignmentDueAt = resolveStudentQuizDueAt(quiz.getId(), studentId);
        if (assignmentDueAt != null) {
            return assignmentDueAt;
        }
        if (quiz.getQuizType() == QuizType.EXAM || quiz.getQuizType() == QuizType.ASSIGNMENT) {
            return quiz.getEndTime();
        }
        return null;
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
