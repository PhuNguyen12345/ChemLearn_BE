package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.core.entity.Teacher;
import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.lms.dto.teacher.TeacherGradeRequestDTO;
import com.example.chemlearn.lms.entity.AttemptAnswer;
import com.example.chemlearn.lms.entity.Quiz;
import com.example.chemlearn.lms.entity.QuizAttempt;
import com.example.chemlearn.lms.entity.QuizQuestion;
import com.example.chemlearn.lms.enums.AttemptStatus;
import com.example.chemlearn.lms.enums.QuestionType;
import com.example.chemlearn.lms.repository.AttemptAnswerRepository;
import com.example.chemlearn.lms.repository.ChapterRepository;
import com.example.chemlearn.lms.repository.ClassStudentLinkRepository;
import com.example.chemlearn.lms.repository.LessonRepository;
import com.example.chemlearn.lms.repository.QuestionBankItemRepository;
import com.example.chemlearn.lms.repository.QuizAttemptRepository;
import com.example.chemlearn.lms.repository.QuizQuestionRepository;
import com.example.chemlearn.lms.repository.QuizRepository;
import com.example.chemlearn.lms.repository.StudyClassAssignmentRepository;
import com.example.chemlearn.lms.repository.StudyClassRepository;
import com.example.chemlearn.lms.repository.UserRepository;
import com.example.chemlearn.lms.service.AutoMailNotificationService;
import com.example.chemlearn.lms.service.StudyClassCodeGenerator;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TeacherServiceImplTest {

    private final ChapterRepository chapterRepository = mock(ChapterRepository.class);
    private final LessonRepository lessonRepository = mock(LessonRepository.class);
    private final QuizRepository quizRepository = mock(QuizRepository.class);
    private final QuizQuestionRepository quizQuestionRepository = mock(QuizQuestionRepository.class);
    private final StudyClassAssignmentRepository studyClassAssignmentRepository = mock(StudyClassAssignmentRepository.class);
    private final QuizAttemptRepository quizAttemptRepository = mock(QuizAttemptRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final QuestionBankItemRepository questionBankItemRepository = mock(QuestionBankItemRepository.class);
    private final ClassStudentLinkRepository classStudentLinkRepository = mock(ClassStudentLinkRepository.class);
    private final StudyClassRepository studyClassRepository = mock(StudyClassRepository.class);
    private final StudyClassCodeGenerator studyClassCodeGenerator = mock(StudyClassCodeGenerator.class);
    private final AttemptAnswerRepository attemptAnswerRepository = mock(AttemptAnswerRepository.class);
    private final EntityManager entityManager = mock(EntityManager.class);
    private final AutoMailNotificationService autoMailNotificationService = mock(AutoMailNotificationService.class);

    private final TeacherServiceImpl service = new TeacherServiceImpl(
            chapterRepository,
            lessonRepository,
            quizRepository,
            quizQuestionRepository,
            studyClassAssignmentRepository,
            quizAttemptRepository,
            userRepository,
            questionBankItemRepository,
            classStudentLinkRepository,
            studyClassRepository,
            studyClassCodeGenerator,
            attemptAnswerRepository,
            entityManager,
            autoMailNotificationService
    );

    @Test
    void gradingEssaysPublishesWeightedFinalScore() {
        UUID teacherId = UUID.randomUUID();
        UUID attemptId = UUID.randomUUID();
        QuizAttempt attempt = attempt(attemptId, quiz(teacherId));
        QuizQuestion objective = question(QuestionType.SINGLE_CHOICE, BigDecimal.valueOf(2));
        QuizQuestion essay = question(QuestionType.ESSAY, BigDecimal.valueOf(3));
        AttemptAnswer objectiveAnswer = answer(attempt, objective, BigDecimal.valueOf(2));
        AttemptAnswer essayAnswer = answer(attempt, essay, null);

        when(userRepository.findByUsername("teacher")).thenReturn(Optional.of(teacherUser(teacherId)));
        when(quizAttemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));
        when(attemptAnswerRepository.findByAttemptId(attemptId)).thenReturn(List.of(objectiveAnswer, essayAnswer));

        TeacherGradeRequestDTO dto = new TeacherGradeRequestDTO();
        TeacherGradeRequestDTO.EssayGradeDTO essayGrade = new TeacherGradeRequestDTO.EssayGradeDTO();
        essayGrade.setQuestionId(essay.getId());
        essayGrade.setAwardedPoints(BigDecimal.valueOf(1.5));
        dto.setEssayGrades(List.of(essayGrade));

        service.gradeSubmission(attemptId, dto, "teacher");

        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.COMPLETED);
        assertThat(attempt.getScore()).isEqualByComparingTo("70.00");
        assertThat(essayAnswer.getAwardedPoints()).isEqualByComparingTo("1.5");
    }

    @Test
    void gradingRejectsEssayPointsAboveQuestionValue() {
        UUID teacherId = UUID.randomUUID();
        UUID attemptId = UUID.randomUUID();
        QuizAttempt attempt = attempt(attemptId, quiz(teacherId));
        QuizQuestion essay = question(QuestionType.ESSAY, BigDecimal.valueOf(3));
        AttemptAnswer essayAnswer = answer(attempt, essay, null);

        when(userRepository.findByUsername("teacher")).thenReturn(Optional.of(teacherUser(teacherId)));
        when(quizAttemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));
        when(attemptAnswerRepository.findByAttemptId(attemptId)).thenReturn(List.of(essayAnswer));

        TeacherGradeRequestDTO dto = new TeacherGradeRequestDTO();
        TeacherGradeRequestDTO.EssayGradeDTO essayGrade = new TeacherGradeRequestDTO.EssayGradeDTO();
        essayGrade.setQuestionId(essay.getId());
        essayGrade.setAwardedPoints(BigDecimal.valueOf(4));
        dto.setEssayGrades(List.of(essayGrade));

        assertThatThrownBy(() -> service.gradeSubmission(attemptId, dto, "teacher"))
                .hasMessageContaining("Awarded points must be between 0");
    }

    private User teacherUser(UUID id) {
        User user = new User();
        user.setId(id);
        user.setUsername("teacher");
        user.setRole(UserRole.ROLE_TEACHER);
        return user;
    }

    private Quiz quiz(UUID teacherId) {
        Teacher teacher = new Teacher();
        teacher.setId(teacherId);
        Quiz quiz = new Quiz();
        quiz.setId(UUID.randomUUID());
        quiz.setTitle("Quiz");
        quiz.setCreatedBy(teacher);
        return quiz;
    }

    private QuizAttempt attempt(UUID id, Quiz quiz) {
        QuizAttempt attempt = new QuizAttempt();
        attempt.setId(id);
        attempt.setQuiz(quiz);
        attempt.setStatus(AttemptStatus.NEEDS_GRADING);
        attempt.setScore(null);
        return attempt;
    }

    private QuizQuestion question(QuestionType type, BigDecimal pointValue) {
        QuizQuestion question = new QuizQuestion();
        question.setId(UUID.randomUUID());
        question.setQuestionType(type);
        question.setPointValue(pointValue);
        question.setPrompt("Question");
        return question;
    }

    private AttemptAnswer answer(QuizAttempt attempt, QuizQuestion question, BigDecimal awardedPoints) {
        AttemptAnswer answer = new AttemptAnswer();
        answer.setAttempt(attempt);
        answer.setQuizQuestion(question);
        answer.setIsCorrect(awardedPoints != null && awardedPoints.compareTo(question.getPointValue()) == 0);
        answer.setAwardedPoints(awardedPoints);
        return answer;
    }
}
