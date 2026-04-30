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
import com.example.chemlearn.lms.entity.Quiz;
import com.example.chemlearn.lms.entity.QuizAttempt;
import com.example.chemlearn.lms.entity.QuizQuestion;
import com.example.chemlearn.lms.entity.ClassStudentLink;
import com.example.chemlearn.lms.entity.StudyClassAssignment;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
                User studentUser = requireStudentUser(studentUsername);
                return findVisibleQuizzes(studentUser.getId()).stream()
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

        List<QuizQuestion> quizQuestions = quizQuestionRepository.findByQuizIdOrderByDisplayOrderAsc(attempt.getQuiz().getId());
        Map<UUID, String> correctOptions = quizQuestions.stream()
                .collect(Collectors.toMap(QuizQuestion::getId, question -> question.getCorrectOption().toUpperCase(), (left, right) -> right, HashMap::new));

        int total = quizQuestions.size();
        int correct = 0;
        for (QuizAnswerDTO answer : requestDTO.getAnswers()) {
            String expected = correctOptions.get(answer.getQuestionId());
            if (expected != null && expected.equalsIgnoreCase(answer.getSelectedOption())) {
                correct++;
            }
        }

        attempt.setCorrectAnswers(correct);
        attempt.setTotalQuestions(total);
        attempt.setScore(total == 0 ? BigDecimal.ZERO : BigDecimal.valueOf((correct * 100.0) / total));
        attempt.setStatus(AttemptStatus.COMPLETED);
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
        if (!isVisibleToStudent(quizId, studentId)) {
            throw new CustomExceptions.ResourceNotFoundException("Quiz not found");
        }
        return quiz;
    }

    private List<Quiz> findVisibleQuizzes(UUID studentId) {
        List<UUID> classIds = classStudentLinkRepository.findByStudentId(studentId)
                .stream()
                .map(ClassStudentLink::getClassRoom)
                .filter(java.util.Objects::nonNull)
                .map(StudyClass -> StudyClass.getId())
                .toList();
        if (classIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, Quiz> quizzesById = assignmentRepository.findByStudyClassField_IdIn(classIds)
                .stream()
                .map(StudyClassAssignment::getQuiz)
                .filter(quiz -> quiz != null && Boolean.TRUE.equals(quiz.getPublished()))
                .collect(Collectors.toMap(Quiz::getId, quiz -> quiz, (left, right) -> left, LinkedHashMap::new));
        return List.copyOf(quizzesById.values());
    }

    private boolean isVisibleToStudent(UUID quizId, UUID studentId) {
        List<UUID> classIds = classStudentLinkRepository.findByStudentId(studentId)
                .stream()
                .map(ClassStudentLink::getClassRoom)
                .filter(java.util.Objects::nonNull)
                .map(StudyClass -> StudyClass.getId())
                .toList();
        if (classIds.isEmpty()) {
            return false;
        }

        return assignmentRepository.findByStudyClassField_IdIn(classIds)
                .stream()
                .anyMatch(assignment -> assignment.getQuiz() != null
                        && quizId.equals(assignment.getQuiz().getId())
                        && Boolean.TRUE.equals(assignment.getQuiz().getPublished()));
    }
}
