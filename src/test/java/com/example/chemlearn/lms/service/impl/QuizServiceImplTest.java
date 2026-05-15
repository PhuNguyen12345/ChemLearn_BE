package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.lms.dto.quiz.QuizAnswerDTO;
import com.example.chemlearn.lms.dto.quiz.QuizSubmitRequestDTO;
import com.example.chemlearn.lms.dto.quiz.QuizSubmitResponseDTO;
import com.example.chemlearn.lms.entity.AttemptAnswer;
import com.example.chemlearn.lms.entity.Quiz;
import com.example.chemlearn.lms.entity.QuizAttempt;
import com.example.chemlearn.lms.entity.QuizQuestion;
import com.example.chemlearn.lms.enums.AttemptStatus;
import com.example.chemlearn.lms.enums.QuestionType;
import com.example.chemlearn.lms.enums.QuizType;
import com.example.chemlearn.lms.repository.AttemptAnswerRepository;
import com.example.chemlearn.lms.repository.ClassStudentLinkRepository;
import com.example.chemlearn.lms.repository.QuizAttemptRepository;
import com.example.chemlearn.lms.repository.QuizQuestionRepository;
import com.example.chemlearn.lms.repository.QuizRepository;
import com.example.chemlearn.lms.repository.StudyClassAssignmentRepository;
import com.example.chemlearn.lms.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizServiceImplTest {

    private final QuizRepository quizRepository = mock(QuizRepository.class);
    private final QuizQuestionRepository quizQuestionRepository = mock(QuizQuestionRepository.class);
    private final QuizAttemptRepository quizAttemptRepository = mock(QuizAttemptRepository.class);
    private final AttemptAnswerRepository attemptAnswerRepository = mock(AttemptAnswerRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ClassStudentLinkRepository classStudentLinkRepository = mock(ClassStudentLinkRepository.class);
    private final StudyClassAssignmentRepository assignmentRepository = mock(StudyClassAssignmentRepository.class);

    private final QuizServiceImpl service = new QuizServiceImpl(
            quizRepository,
            quizQuestionRepository,
            quizAttemptRepository,
            attemptAnswerRepository,
            userRepository,
            classStudentLinkRepository,
            assignmentRepository
    );

    @Test
    void objectiveOnlyQuizPublishesWeightedScoreImmediately() {
        UUID studentId = UUID.randomUUID();
        UUID attemptId = UUID.randomUUID();
        UUID quizId = UUID.randomUUID();
        QuizAttempt attempt = attempt(attemptId, quiz(quizId), student(studentId));
        QuizQuestion q1 = question(quizId, QuestionType.SINGLE_CHOICE, "A", BigDecimal.valueOf(2));
        QuizQuestion q2 = question(quizId, QuestionType.SINGLE_CHOICE, "B", BigDecimal.ONE);

        when(userRepository.findByUsername("student")).thenReturn(Optional.of(studentUser(studentId)));
        when(quizAttemptRepository.findByIdAndStudentId(attemptId, studentId)).thenReturn(Optional.of(attempt));
        when(quizQuestionRepository.findByQuizIdOrderByDisplayOrderAsc(quizId)).thenReturn(List.of(q1, q2));
        when(quizQuestionRepository.existsByQuizIdAndQuestionType(quizId, QuestionType.ESSAY)).thenReturn(false);

        QuizSubmitResponseDTO response = service.submitAttempt(
                attemptId,
                request(answer(q1.getId(), "A"), answer(q2.getId(), "C")),
                "student"
        );

        assertThat(response.getStatus()).isEqualTo(AttemptStatus.COMPLETED);
        assertThat(attempt.getScore()).isEqualByComparingTo("66.67");
        assertThat(response.getScore()).isEqualTo(66);
    }

    @Test
    void essayOnlyQuizWaitsForTeacherAndDoesNotPublishScore() {
        UUID studentId = UUID.randomUUID();
        UUID attemptId = UUID.randomUUID();
        UUID quizId = UUID.randomUUID();
        QuizAttempt attempt = attempt(attemptId, quiz(quizId), student(studentId));
        QuizQuestion essay = question(quizId, QuestionType.ESSAY, null, BigDecimal.valueOf(5));

        when(userRepository.findByUsername("student")).thenReturn(Optional.of(studentUser(studentId)));
        when(quizAttemptRepository.findByIdAndStudentId(attemptId, studentId)).thenReturn(Optional.of(attempt));
        when(quizQuestionRepository.findByQuizIdOrderByDisplayOrderAsc(quizId)).thenReturn(List.of(essay));
        when(quizQuestionRepository.existsByQuizIdAndQuestionType(quizId, QuestionType.ESSAY)).thenReturn(true);

        QuizSubmitResponseDTO response = service.submitAttempt(
                attemptId,
                request(answer(essay.getId(), "essay response")),
                "student"
        );

        assertThat(response.getStatus()).isEqualTo(AttemptStatus.NEEDS_GRADING);
        assertThat(response.getScore()).isNull();
        assertThat(attempt.getScore()).isNull();
    }

    @Test
    void mixedQuizStoresObjectivePointsButKeepsFinalScoreUnpublished() {
        UUID studentId = UUID.randomUUID();
        UUID attemptId = UUID.randomUUID();
        UUID quizId = UUID.randomUUID();
        QuizAttempt attempt = attempt(attemptId, quiz(quizId), student(studentId));
        QuizQuestion objective = question(quizId, QuestionType.SINGLE_CHOICE, "A", BigDecimal.valueOf(2));
        QuizQuestion essay = question(quizId, QuestionType.ESSAY, null, BigDecimal.valueOf(3));

        when(userRepository.findByUsername("student")).thenReturn(Optional.of(studentUser(studentId)));
        when(quizAttemptRepository.findByIdAndStudentId(attemptId, studentId)).thenReturn(Optional.of(attempt));
        when(quizQuestionRepository.findByQuizIdOrderByDisplayOrderAsc(quizId)).thenReturn(List.of(objective, essay));
        when(quizQuestionRepository.existsByQuizIdAndQuestionType(quizId, QuestionType.ESSAY)).thenReturn(true);

        service.submitAttempt(attemptId, request(answer(objective.getId(), "A"), answer(essay.getId(), "essay")), "student");

        ArgumentCaptor<AttemptAnswer> answerCaptor = ArgumentCaptor.forClass(AttemptAnswer.class);
        org.mockito.Mockito.verify(attemptAnswerRepository, org.mockito.Mockito.times(2)).save(answerCaptor.capture());
        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.NEEDS_GRADING);
        assertThat(attempt.getScore()).isNull();
        assertThat(answerCaptor.getAllValues())
                .filteredOn(answer -> answer.getQuizQuestion().getId().equals(objective.getId()))
                .singleElement()
                .extracting(AttemptAnswer::getAwardedPoints)
                .isEqualTo(BigDecimal.valueOf(2));
    }

    private User studentUser(UUID id) {
        User user = new User();
        user.setId(id);
        user.setUsername("student");
        user.setRole(UserRole.ROLE_STUDENT);
        return user;
    }

    private Student student(UUID id) {
        Student student = new Student();
        student.setId(id);
        return student;
    }

    private Quiz quiz(UUID id) {
        Quiz quiz = new Quiz();
        quiz.setId(id);
        quiz.setQuizType(QuizType.FREE);
        quiz.setPublished(true);
        quiz.setTitle("Quiz");
        return quiz;
    }

    private QuizAttempt attempt(UUID id, Quiz quiz, Student student) {
        QuizAttempt attempt = new QuizAttempt();
        attempt.setId(id);
        attempt.setQuiz(quiz);
        attempt.setStudent(student);
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        attempt.setScore(BigDecimal.ZERO);
        return attempt;
    }

    private QuizQuestion question(UUID quizId, QuestionType type, String correctOption, BigDecimal pointValue) {
        QuizQuestion question = new QuizQuestion();
        question.setId(UUID.randomUUID());
        question.setQuiz(quiz(quizId));
        question.setQuestionType(type);
        question.setPrompt("Question");
        question.setCorrectOption(correctOption);
        question.setPointValue(pointValue);
        return question;
    }

    private QuizAnswerDTO answer(UUID questionId, String selectedOption) {
        QuizAnswerDTO answer = new QuizAnswerDTO();
        answer.setQuestionId(questionId);
        answer.setSelectedOption(selectedOption);
        return answer;
    }

    private QuizSubmitRequestDTO request(QuizAnswerDTO... answers) {
        QuizSubmitRequestDTO request = new QuizSubmitRequestDTO();
        request.setAnswers(List.of(answers));
        return request;
    }
}
