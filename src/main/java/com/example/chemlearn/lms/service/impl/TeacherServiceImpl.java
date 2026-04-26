package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.core.entity.Teacher;
import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.lms.dto.teacher.TeacherAssignmentRequestDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherChapterRequestDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherClassInfoDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherDashboardSummaryDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherLessonRequestDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherQuestionBankItemDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherQuestionBankRequestDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherQuizQuestionRequestDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherQuizRequestDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherStudentAccountDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherStudentPerformanceDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherSubmissionDTO;
import com.example.chemlearn.lms.entity.Assignment;
import com.example.chemlearn.lms.entity.Chapter;
import com.example.chemlearn.lms.entity.ClassStudentLink;
import com.example.chemlearn.lms.entity.Lesson;
import com.example.chemlearn.lms.entity.QuestionBankItem;
import com.example.chemlearn.lms.entity.Quiz;
import com.example.chemlearn.lms.entity.QuizAttempt;
import com.example.chemlearn.lms.entity.QuizQuestion;
import com.example.chemlearn.lms.repository.UserRepository;
import com.example.chemlearn.lms.repository.AssignmentRepository;
import com.example.chemlearn.lms.repository.ChapterRepository;
import com.example.chemlearn.lms.repository.ClassStudentLinkRepository;
import com.example.chemlearn.lms.repository.LessonRepository;
import com.example.chemlearn.lms.repository.QuestionBankItemRepository;
import com.example.chemlearn.lms.repository.QuizAttemptRepository;
import com.example.chemlearn.lms.repository.QuizQuestionRepository;
import com.example.chemlearn.lms.repository.QuizRepository;
import com.example.chemlearn.lms.repository.StudyClassRepository;
import com.example.chemlearn.lms.service.TeacherService;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {
    private final ChapterRepository chapterRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final AssignmentRepository assignmentRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserRepository userRepository;
    private final QuestionBankItemRepository questionBankItemRepository;
    private final ClassStudentLinkRepository classStudentLinkRepository;
    private final StudyClassRepository studyClassRepository;
    private final EntityManager entityManager;

    @Override public List<Chapter> getChapters() { return chapterRepository.findAll(); }

    @Override
    public Chapter createChapter(TeacherChapterRequestDTO dto) {
        Chapter chapter = new Chapter();
        chapter.setTitle(dto.getTitle());
        chapter.setDescription(dto.getDescription());
        chapter.setGradeLevel(10);
        chapter.setOrderIndex(dto.getDisplayOrder() == null ? 0 : dto.getDisplayOrder());
        chapter.setPublished(dto.getPublished() == null ? Boolean.TRUE : dto.getPublished());
        chapter.setCreatedAt(Instant.now());
        chapter.setUpdatedAt(Instant.now());
        return chapterRepository.save(chapter);
    }

    @Override
    public Chapter updateChapter(UUID chapterId, TeacherChapterRequestDTO dto) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));
        chapter.setTitle(dto.getTitle());
        chapter.setDescription(dto.getDescription());
        chapter.setOrderIndex(dto.getDisplayOrder() == null ? 0 : dto.getDisplayOrder());
        chapter.setPublished(dto.getPublished() == null ? Boolean.TRUE : dto.getPublished());
        chapter.setUpdatedAt(Instant.now());
        return chapterRepository.save(chapter);
    }

    @Override
    public void deleteChapter(UUID chapterId) {
        chapterRepository.deleteById(chapterId);
    }

    @Override public List<Lesson> getLessons() { return lessonRepository.findAll(); }

    @Override
    public Lesson createLesson(TeacherLessonRequestDTO dto) {
        Chapter chapter = chapterRepository.findById(dto.getChapterId())
                .orElseThrow(() -> new RuntimeException("Chapter not found"));
        Lesson lesson = new Lesson();
        lesson.setChapter(chapter);
        lesson.setTitle(dto.getTitle());
        lesson.setTextContent(dto.getContent());
        lesson.setContentType("TEXT");
        lesson.setDurationMinutes(dto.getEstimatedMinutes() == null ? 0 : dto.getEstimatedMinutes());
        lesson.setOrderIndex(dto.getDisplayOrder() == null ? 0 : dto.getDisplayOrder());
        lesson.setPublished(dto.getPublished() == null ? Boolean.TRUE : dto.getPublished());
        lesson.setCreatedAt(Instant.now());
        lesson.setUpdatedAt(Instant.now());
        return lessonRepository.save(lesson);
    }

    @Override
    public Lesson updateLesson(UUID lessonId, TeacherLessonRequestDTO dto) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        Chapter chapter = chapterRepository.findById(dto.getChapterId())
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        lesson.setChapter(chapter);
        lesson.setTitle(dto.getTitle());
        lesson.setTextContent(dto.getContent());
        lesson.setDurationMinutes(dto.getEstimatedMinutes() == null ? 0 : dto.getEstimatedMinutes());
        lesson.setOrderIndex(dto.getDisplayOrder() == null ? 0 : dto.getDisplayOrder());
        lesson.setPublished(dto.getPublished() == null ? Boolean.TRUE : dto.getPublished());
        lesson.setUpdatedAt(Instant.now());
        return lessonRepository.save(lesson);
    }

    @Override
    public void deleteLesson(UUID lessonId) {
        lessonRepository.deleteById(lessonId);
    }

    @Override
    public List<Quiz> getQuizzes(String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        return quizRepository.findByCreatedByIdOrderByIdDesc(teacherId);
    }

    @Override
    public Quiz createQuiz(TeacherQuizRequestDTO dto, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        Quiz quiz = new Quiz();
        quiz.setTitle(dto.getTitle());
        quiz.setDescription(dto.getDescription());
        quiz.setQuizType(dto.getQuizType());
        quiz.setDurationMinutes(dto.getDurationMinutes());
        quiz.setPublished(dto.getPublished() == null ? Boolean.TRUE : dto.getPublished());
        quiz.setCreatedAt(Instant.now());
        quiz.setCreatedBy(entityManager.getReference(Teacher.class, teacherId));
        return quizRepository.save(quiz);
    }

    @Override
    public Quiz updateQuiz(UUID quizId, TeacherQuizRequestDTO dto, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        Quiz quiz = requireOwnedQuiz(quizId, teacherId);
        quiz.setTitle(dto.getTitle());
        quiz.setDescription(dto.getDescription());
        quiz.setQuizType(dto.getQuizType());
        quiz.setDurationMinutes(dto.getDurationMinutes());
        quiz.setPublished(dto.getPublished() == null ? Boolean.TRUE : dto.getPublished());
        return quizRepository.save(quiz);
    }

    @Override
    public void deleteQuiz(UUID quizId, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        Quiz quiz = requireOwnedQuiz(quizId, teacherId);
        quizRepository.delete(quiz);
    }

    @Override
    public List<QuizQuestion> getQuizQuestions(UUID quizId, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        requireOwnedQuiz(quizId, teacherId);
        return quizQuestionRepository.findByQuizIdOrderByDisplayOrderAsc(quizId);
    }

    @Override
    public List<TeacherQuestionBankItemDTO> getQuestionBank(String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        return questionBankItemRepository.findByCreatedByIdOrderByCreatedAtDesc(teacherId)
                .stream()
                .map(this::toQuestionBankDto)
                .toList();
    }

    @Override
    public TeacherQuestionBankItemDTO createQuestionBankItem(TeacherQuestionBankRequestDTO dto, String teacherUsername) {
        User teacherUser = requireTeacherUser(teacherUsername);
        QuestionBankItem item = new QuestionBankItem();
        item.setCreatedBy(teacherUser);
        item.setPrompt(dto.getPrompt());
        item.setOptionA(dto.getOptionA());
        item.setOptionB(dto.getOptionB());
        item.setOptionC(dto.getOptionC());
        item.setOptionD(dto.getOptionD());
        item.setCorrectOption(dto.getCorrectOption());
        item.setExplanation(dto.getExplanation());
        item.setCreatedAt(LocalDateTime.now());
        return toQuestionBankDto(questionBankItemRepository.save(item));
    }

    @Override
    public TeacherQuestionBankItemDTO updateQuestionBankItem(UUID bankQuestionId, TeacherQuestionBankRequestDTO dto, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        QuestionBankItem item = questionBankItemRepository.findByIdAndCreatedById(bankQuestionId, teacherId)
                .orElseThrow(() -> new RuntimeException("Question bank item not found"));
        item.setPrompt(dto.getPrompt());
        item.setOptionA(dto.getOptionA());
        item.setOptionB(dto.getOptionB());
        item.setOptionC(dto.getOptionC());
        item.setOptionD(dto.getOptionD());
        item.setCorrectOption(dto.getCorrectOption());
        item.setExplanation(dto.getExplanation());
        return toQuestionBankDto(questionBankItemRepository.save(item));
    }

    @Override
    public void deleteQuestionBankItem(UUID bankQuestionId, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        QuestionBankItem item = questionBankItemRepository.findByIdAndCreatedById(bankQuestionId, teacherId)
                .orElseThrow(() -> new RuntimeException("Question bank item not found"));
        questionBankItemRepository.delete(item);
    }

    @Override
    @Transactional
    public QuizQuestion addQuestionFromBank(UUID quizId, UUID bankQuestionId, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        Quiz quiz = requireOwnedQuiz(quizId, teacherId);
        QuestionBankItem item = questionBankItemRepository.findByIdAndCreatedById(bankQuestionId, teacherId)
                .orElseThrow(() -> new RuntimeException("Question bank item not found"));

        QuizQuestion question = new QuizQuestion();
        question.setQuiz(quiz);
        question.setPrompt(item.getPrompt());
        question.setOptionA(item.getOptionA());
        question.setOptionB(item.getOptionB());
        question.setOptionC(item.getOptionC());
        question.setOptionD(item.getOptionD());
        question.setCorrectOption(item.getCorrectOption());
        question.setExplanation(item.getExplanation());
        question.setDisplayOrder((int) quizQuestionRepository.countByQuizId(quizId) + 1);
        return quizQuestionRepository.save(question);
    }

    @Override
    public QuizQuestion createQuizQuestion(UUID quizId, TeacherQuizQuestionRequestDTO dto, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        Quiz quiz = requireOwnedQuiz(quizId, teacherId);
        QuizQuestion question = new QuizQuestion();
        question.setQuiz(quiz);
        question.setPrompt(dto.getPrompt());
        question.setOptionA(dto.getOptionA());
        question.setOptionB(dto.getOptionB());
        question.setOptionC(dto.getOptionC());
        question.setOptionD(dto.getOptionD());
        question.setCorrectOption(dto.getCorrectOption());
        question.setExplanation(dto.getExplanation());
        question.setDisplayOrder(dto.getDisplayOrder() == null ? (int) quizQuestionRepository.countByQuizId(quizId) + 1 : dto.getDisplayOrder());
        return quizQuestionRepository.save(question);
    }

    @Override
    public QuizQuestion updateQuizQuestion(UUID questionId, TeacherQuizQuestionRequestDTO dto, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        QuizQuestion question = quizQuestionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Quiz question not found"));
        UUID ownerId = question.getQuiz().getCreatedBy() == null ? null : question.getQuiz().getCreatedBy().getId();
        if (ownerId == null || !ownerId.equals(teacherId)) {
            throw new RuntimeException("You are not allowed to modify this question");
        }

        question.setPrompt(dto.getPrompt());
        question.setOptionA(dto.getOptionA());
        question.setOptionB(dto.getOptionB());
        question.setOptionC(dto.getOptionC());
        question.setOptionD(dto.getOptionD());
        question.setCorrectOption(dto.getCorrectOption());
        question.setExplanation(dto.getExplanation());
        question.setDisplayOrder(dto.getDisplayOrder() == null ? question.getDisplayOrder() : dto.getDisplayOrder());
        return quizQuestionRepository.save(question);
    }

    @Override
    public void deleteQuizQuestion(UUID questionId, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        QuizQuestion question = quizQuestionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Quiz question not found"));
        UUID ownerId = question.getQuiz().getCreatedBy() == null ? null : question.getQuiz().getCreatedBy().getId();
        if (ownerId == null || !ownerId.equals(teacherId)) {
            throw new RuntimeException("You are not allowed to modify this question");
        }
        quizQuestionRepository.delete(question);
    }

    @Override
    public List<Assignment> getAssignments(String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        return assignmentRepository.findByTeacherIdOrderByIdDesc(teacherId);
    }

    @Override
    public Assignment createAssignment(TeacherAssignmentRequestDTO dto, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        Quiz quiz = requireOwnedQuiz(dto.getQuizId(), teacherId);
        User student = userRepository.findByIdAndRole(dto.getStudentId(), UserRole.ROLE_STUDENT)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Assignment assignment = new Assignment();
        assignment.setTitle(dto.getTitle());
        assignment.setQuiz(quiz);
        assignment.setTeacher(userRepository.getReferenceById(teacherId));
        assignment.setStudent(student);
        assignment.setDueAt(dto.getDueAt());
        return assignmentRepository.save(assignment);
    }

    @Override
    public Assignment updateAssignment(UUID assignmentId, TeacherAssignmentRequestDTO dto, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        if (!assignment.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("You are not allowed to modify this assignment");
        }

        Quiz quiz = requireOwnedQuiz(dto.getQuizId(), teacherId);
        User student = userRepository.findByIdAndRole(dto.getStudentId(), UserRole.ROLE_STUDENT)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        assignment.setTitle(dto.getTitle());
        assignment.setQuiz(quiz);
        assignment.setStudent(student);
        assignment.setDueAt(dto.getDueAt());
        return assignmentRepository.save(assignment);
    }

    @Override
    public void deleteAssignment(UUID assignmentId, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        if (!assignment.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("You are not allowed to delete this assignment");
        }
        assignmentRepository.delete(assignment);
    }

        @Override
        public List<TeacherSubmissionDTO> getSubmissions(String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        return quizAttemptRepository.findByQuizCreatedByIdOrderByStartedAtDesc(teacherId)
            .stream()
            .map(attempt -> new TeacherSubmissionDTO(
                attempt.getId(),
                attempt.getQuiz() == null ? "Unknown Quiz" : attempt.getQuiz().getTitle(),
                attempt.getStudent() == null ? null : attempt.getStudent().getId(),
                attempt.getStudent() == null || attempt.getStudent().getUsers() == null ? "Unknown Student" : attempt.getStudent().getUsers().getFullName(),
                toIntScore(attempt.getScore()),
                attempt.getStatus(),
                attempt.getSubmittedAt()))
            .toList();
        }

        @Override
        public List<TeacherClassInfoDTO> getAssignedClasses(String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();

        Map<UUID, List<TeacherClassInfoDTO.StudentBrief>> studentsByClassId = classStudentLinkRepository
            .findByClassRoomTeacherIdOrderByClassRoomNameAsc(teacherId)
            .stream()
            .collect(java.util.stream.Collectors.groupingBy(
                link -> link.getClassRoom().getId(),
                LinkedHashMap::new,
                java.util.stream.Collectors.mapping(
                    link -> new TeacherClassInfoDTO.StudentBrief(
                        link.getStudent().getId(),
                        link.getStudent().getUsername(),
                        link.getStudent().getEmail(),
                        "/admin/users/" + link.getStudent().getId()),
                    java.util.stream.Collectors.toList())
            ));

        return studyClassRepository.findByTeacherId(teacherId)
            .stream()
            .map(classRoom -> new TeacherClassInfoDTO(
                classRoom.getId(),
                classRoom.getName(),
                classRoom.getSchedule(),
                classRoom.getDescription(),
                studentsByClassId.getOrDefault(classRoom.getId(), List.of())))
            .toList();
        }

        @Override
        public TeacherStudentAccountDTO getStudentAccount(UUID studentId, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        User student = userRepository.findByIdAndRole(studentId, UserRole.ROLE_STUDENT)
            .orElseThrow(() -> new RuntimeException("Student account not found"));

        boolean assignedToTeacher = classStudentLinkRepository.existsByStudentIdAndClassRoomTeacherId(studentId, teacherId);
        if (!assignedToTeacher) {
            throw new RuntimeException("Student is not assigned to this teacher");
        }

        List<String> classNames = classStudentLinkRepository.findByStudentIdAndClassRoomTeacherId(studentId, teacherId)
            .stream()
            .map(link -> link.getClassRoom().getName())
            .distinct()
            .toList();

        return new TeacherStudentAccountDTO(
            student.getId(),
            student.getUsername(),
            student.getEmail(),
            Boolean.TRUE.equals(student.getIsActive()),
            classNames);
        }

        @Override
        public List<TeacherStudentPerformanceDTO> getStudentPerformance(String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();

        Map<UUID, User> studentsById = classStudentLinkRepository.findByClassRoomTeacherIdOrderByClassRoomNameAsc(teacherId)
            .stream()
            .map(ClassStudentLink::getStudent)
            .collect(java.util.stream.Collectors.toMap(User::getId, student -> student, (first, second) -> first, LinkedHashMap::new));

        if (studentsById.isEmpty()) {
            return List.of();
        }

        Map<UUID, List<QuizAttempt>> attemptsByStudentId = quizAttemptRepository.findByQuizCreatedByIdOrderByStartedAtDesc(teacherId)
            .stream()
            .filter(attempt -> attempt.getStudent() != null)
            .collect(java.util.stream.Collectors.groupingBy(attempt -> attempt.getStudent().getId()));

        Map<UUID, List<Assignment>> assignmentsByStudentId = assignmentRepository.findByTeacherIdOrderByIdDesc(teacherId)
            .stream()
            .collect(java.util.stream.Collectors.groupingBy(assignment -> assignment.getStudent().getId()));

        return studentsById.values().stream()
            .map(student -> {
                List<QuizAttempt> attempts = attemptsByStudentId.getOrDefault(student.getId(), List.of());
                int attemptsCount = attempts.size();
                int averageScore = attemptsCount == 0
                    ? 0
                    : (int) Math.round(
                        attempts.stream()
                            .map(QuizAttempt::getScore)
                            .filter(java.util.Objects::nonNull)
                            .mapToDouble(BigDecimal::doubleValue)
                            .average()
                            .orElse(0));

                List<Assignment> assignments = assignmentsByStudentId.getOrDefault(student.getId(), List.of());
                long completed = assignments.stream()
                    .filter(assignment -> assignment.getStatus() == com.example.chemlearn.lms.enums.AssignmentStatus.SUBMITTED
                        || assignment.getStatus() == com.example.chemlearn.lms.enums.AssignmentStatus.REVIEWED)
                    .count();
                long pending = Math.max(0, assignments.size() - completed);

                return new TeacherStudentPerformanceDTO(
                    student.getId(),
                    student.getFullName() == null || student.getFullName().isBlank() ? student.getUsername() : student.getFullName(),
                    attemptsCount,
                    averageScore,
                    completed,
                    pending);
            })
            .sorted(Comparator.comparing(TeacherStudentPerformanceDTO::getStudentName, String.CASE_INSENSITIVE_ORDER))
            .toList();
        }

        @Override
        public TeacherDashboardSummaryDTO getSummary(String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        List<TeacherStudentPerformanceDTO> performance = getStudentPerformance(teacherUsername);
        int totalStudents = performance.size();
        int averageScore = totalStudents == 0
            ? 0
            : (int) Math.round(performance.stream().mapToInt(TeacherStudentPerformanceDTO::getAverageScore).average().orElse(0));
        long pendingAssignments = assignmentRepository.findByTeacherIdOrderByIdDesc(teacherId)
            .stream()
            .filter(assignment -> assignment.getStatus() != com.example.chemlearn.lms.enums.AssignmentStatus.SUBMITTED
                && assignment.getStatus() != com.example.chemlearn.lms.enums.AssignmentStatus.REVIEWED)
            .count();

        return new TeacherDashboardSummaryDTO(
            totalStudents,
            averageScore,
            lessonRepository.count(),
            (long) quizRepository.findByCreatedByIdOrderByIdDesc(teacherId).size(),
            pendingAssignments);
        }

    private User requireTeacherUser(String teacherUsername) {
        User user = userRepository.findByUsername(teacherUsername)
                .orElseThrow(() -> new RuntimeException("Teacher account not found"));
        if (user.getRole() != UserRole.ROLE_TEACHER) {
            throw new RuntimeException("Current account is not a teacher");
        }
        return user;
    }

    private Quiz requireOwnedQuiz(UUID quizId, UUID teacherId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        UUID ownerId = quiz.getCreatedBy() == null ? null : quiz.getCreatedBy().getId();
        if (ownerId == null || !ownerId.equals(teacherId)) {
            throw new RuntimeException("You are not allowed to modify this quiz");
        }
        return quiz;
    }

    private TeacherQuestionBankItemDTO toQuestionBankDto(QuestionBankItem item) {
        return new TeacherQuestionBankItemDTO(
                item.getId(),
                item.getPrompt(),
                item.getOptionA(),
                item.getOptionB(),
                item.getOptionC(),
                item.getOptionD(),
                item.getCorrectOption(),
                item.getExplanation(),
                item.getCreatedAt());
    }

    private int toIntScore(BigDecimal score) {
        if (score == null) {
            return 0;
        }
        return (int) Math.round(score.doubleValue());
    }
}