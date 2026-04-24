package com.example.chemlearn.service.serviceImpl;

import com.example.chemlearn.dtos.teacher.*;
import com.example.chemlearn.entity.*;
import com.example.chemlearn.enums.AccountRole;
import com.example.chemlearn.enums.AssignmentStatus;
import com.example.chemlearn.exception.CustomExceptions;
import com.example.chemlearn.repository.*;
import com.example.chemlearn.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final ChapterRepository chapterRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final AssignmentRepository assignmentRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final AccountRepository accountRepository;
    private final QuestionBankItemRepository questionBankItemRepository;
    private final ChemClassRepository chemClassRepository;
    private final ClassStudentLinkRepository classStudentLinkRepository;

    @Override
    public List<Chapter> getChapters() {
        return chapterRepository.findAll();
    }

    @Override
    public Chapter createChapter(TeacherChapterRequestDTO dto) {
        Chapter chapter = new Chapter();
        chapter.setTitle(dto.getTitle());
        chapter.setDescription(dto.getDescription());
        chapter.setDisplayOrder(dto.getDisplayOrder() == null ? 0 : dto.getDisplayOrder());
        chapter.setPublished(dto.getPublished() == null || dto.getPublished());
        return chapterRepository.save(chapter);
    }

    @Override
    public Chapter updateChapter(Long chapterId, TeacherChapterRequestDTO dto) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Chapter not found"));
        chapter.setTitle(dto.getTitle());
        chapter.setDescription(dto.getDescription());
        chapter.setDisplayOrder(dto.getDisplayOrder() == null ? chapter.getDisplayOrder() : dto.getDisplayOrder());
        chapter.setPublished(dto.getPublished() == null ? chapter.isPublished() : dto.getPublished());
        return chapterRepository.save(chapter);
    }

    @Override
    public void deleteChapter(Long chapterId) {
        if (!chapterRepository.existsById(chapterId)) {
            throw new CustomExceptions.ResourceNotFoundException("Chapter not found");
        }
        chapterRepository.deleteById(chapterId);
    }

    @Override
    public List<Lesson> getLessons() {
        return lessonRepository.findAll();
    }

    @Override
    public Lesson createLesson(TeacherLessonRequestDTO dto) {
        Chapter chapter = chapterRepository.findById(dto.getChapterId())
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Chapter not found"));

        Lesson lesson = new Lesson();
        lesson.setChapter(chapter);
        lesson.setTitle(dto.getTitle());
        lesson.setContent(dto.getContent());
        lesson.setEstimatedMinutes(dto.getEstimatedMinutes());
        lesson.setDisplayOrder(dto.getDisplayOrder() == null ? 0 : dto.getDisplayOrder());
        lesson.setPublished(dto.getPublished() == null || dto.getPublished());
        return lessonRepository.save(lesson);
    }

    @Override
    public Lesson updateLesson(Long lessonId, TeacherLessonRequestDTO dto) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Lesson not found"));
        Chapter chapter = chapterRepository.findById(dto.getChapterId())
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Chapter not found"));

        lesson.setChapter(chapter);
        lesson.setTitle(dto.getTitle());
        lesson.setContent(dto.getContent());
        lesson.setEstimatedMinutes(dto.getEstimatedMinutes());
        lesson.setDisplayOrder(dto.getDisplayOrder() == null ? lesson.getDisplayOrder() : dto.getDisplayOrder());
        lesson.setPublished(dto.getPublished() == null ? lesson.isPublished() : dto.getPublished());
        return lessonRepository.save(lesson);
    }

    @Override
    public void deleteLesson(Long lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new CustomExceptions.ResourceNotFoundException("Lesson not found");
        }
        lessonRepository.deleteById(lessonId);
    }

    @Override
    public List<Quiz> getQuizzes(String teacherUsername) {
        Account teacher = getTeacherByUsername(teacherUsername);
        return quizRepository.findByCreatedByIdOrderByIdDesc(teacher.getId());
    }

    @Override
    public Quiz createQuiz(TeacherQuizRequestDTO dto, String teacherUsername) {
        Account teacher = getTeacherByUsername(teacherUsername);
        Quiz quiz = new Quiz();
        quiz.setTitle(dto.getTitle());
        quiz.setDescription(dto.getDescription());
        quiz.setQuizType(dto.getQuizType());
        quiz.setDurationMinutes(dto.getDurationMinutes());
        quiz.setPublished(dto.getPublished() == null || dto.getPublished());
        quiz.setCreatedBy(teacher);
        return quizRepository.save(quiz);
    }

    @Override
    public Quiz updateQuiz(Long quizId, TeacherQuizRequestDTO dto, String teacherUsername) {
        Quiz quiz = getOwnedQuiz(quizId, teacherUsername);
        quiz.setTitle(dto.getTitle());
        quiz.setDescription(dto.getDescription());
        quiz.setQuizType(dto.getQuizType());
        quiz.setDurationMinutes(dto.getDurationMinutes());
        quiz.setPublished(dto.getPublished() == null ? quiz.isPublished() : dto.getPublished());
        return quizRepository.save(quiz);
    }

    @Override
    public void deleteQuiz(Long quizId, String teacherUsername) {
        Quiz quiz = getOwnedQuiz(quizId, teacherUsername);
        quizRepository.delete(quiz);
    }

    @Override
    public List<QuizQuestion> getQuizQuestions(Long quizId, String teacherUsername) {
        Quiz quiz = getOwnedQuiz(quizId, teacherUsername);
        return quizQuestionRepository.findByQuizIdOrderByDisplayOrderAsc(quiz.getId());
    }

    @Override
    public List<TeacherQuestionBankItemDTO> getQuestionBank(String teacherUsername) {
        Account teacher = getTeacherByUsername(teacherUsername);
        return questionBankItemRepository.findByCreatedByIdOrderByCreatedAtDesc(teacher.getId())
                .stream()
                .map(this::toQuestionBankItemDto)
                .toList();
    }

    @Override
    public TeacherQuestionBankItemDTO createQuestionBankItem(TeacherQuestionBankRequestDTO dto, String teacherUsername) {
        Account teacher = getTeacherByUsername(teacherUsername);

        QuestionBankItem item = new QuestionBankItem();
        item.setCreatedBy(teacher);
        item.setPrompt(dto.getPrompt());
        item.setOptionA(dto.getOptionA());
        item.setOptionB(dto.getOptionB());
        item.setOptionC(dto.getOptionC());
        item.setOptionD(dto.getOptionD());
        item.setCorrectOption(dto.getCorrectOption().toUpperCase());
        item.setExplanation(dto.getExplanation());
        item.setCreatedAt(LocalDateTime.now());

        return toQuestionBankItemDto(questionBankItemRepository.save(item));
    }

    @Override
    public TeacherQuestionBankItemDTO updateQuestionBankItem(Long bankQuestionId, TeacherQuestionBankRequestDTO dto, String teacherUsername) {
        Account teacher = getTeacherByUsername(teacherUsername);

        QuestionBankItem item = questionBankItemRepository.findByIdAndCreatedById(bankQuestionId, teacher.getId())
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Question bank item not found"));

        item.setPrompt(dto.getPrompt());
        item.setOptionA(dto.getOptionA());
        item.setOptionB(dto.getOptionB());
        item.setOptionC(dto.getOptionC());
        item.setOptionD(dto.getOptionD());
        item.setCorrectOption(dto.getCorrectOption().toUpperCase());
        item.setExplanation(dto.getExplanation());

        return toQuestionBankItemDto(questionBankItemRepository.save(item));
    }

    @Override
    public void deleteQuestionBankItem(Long bankQuestionId, String teacherUsername) {
        Account teacher = getTeacherByUsername(teacherUsername);

        QuestionBankItem item = questionBankItemRepository.findByIdAndCreatedById(bankQuestionId, teacher.getId())
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Question bank item not found"));

        questionBankItemRepository.delete(item);
    }

    @Override
    public QuizQuestion addQuestionFromBank(Long quizId, Long bankQuestionId, String teacherUsername) {
        Quiz quiz = getOwnedQuiz(quizId, teacherUsername);
        Account teacher = getTeacherByUsername(teacherUsername);

        QuestionBankItem item = questionBankItemRepository.findByIdAndCreatedById(bankQuestionId, teacher.getId())
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Question bank item not found"));

        QuizQuestion question = new QuizQuestion();
        question.setQuiz(quiz);
        question.setPrompt(item.getPrompt());
        question.setOptionA(item.getOptionA());
        question.setOptionB(item.getOptionB());
        question.setOptionC(item.getOptionC());
        question.setOptionD(item.getOptionD());
        question.setCorrectOption(item.getCorrectOption());
        question.setExplanation(item.getExplanation());
        question.setDisplayOrder((int) quizQuestionRepository.countByQuizId(quiz.getId()));

        return quizQuestionRepository.save(question);
    }

    @Override
    public QuizQuestion createQuizQuestion(Long quizId, TeacherQuizQuestionRequestDTO dto, String teacherUsername) {
        Quiz quiz = getOwnedQuiz(quizId, teacherUsername);

        QuizQuestion question = new QuizQuestion();
        question.setQuiz(quiz);
        question.setPrompt(dto.getPrompt());
        question.setOptionA(dto.getOptionA());
        question.setOptionB(dto.getOptionB());
        question.setOptionC(dto.getOptionC());
        question.setOptionD(dto.getOptionD());
        question.setCorrectOption(dto.getCorrectOption().toUpperCase());
        question.setExplanation(dto.getExplanation());
        question.setDisplayOrder(dto.getDisplayOrder() == null ? 0 : dto.getDisplayOrder());
        return quizQuestionRepository.save(question);
    }

    @Override
    public QuizQuestion updateQuizQuestion(Long questionId, TeacherQuizQuestionRequestDTO dto, String teacherUsername) {
        QuizQuestion question = quizQuestionRepository.findById(questionId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Quiz question not found"));
        ensureOwnedByTeacher(question.getQuiz(), teacherUsername);

        question.setPrompt(dto.getPrompt());
        question.setOptionA(dto.getOptionA());
        question.setOptionB(dto.getOptionB());
        question.setOptionC(dto.getOptionC());
        question.setOptionD(dto.getOptionD());
        question.setCorrectOption(dto.getCorrectOption().toUpperCase());
        question.setExplanation(dto.getExplanation());
        question.setDisplayOrder(dto.getDisplayOrder() == null ? question.getDisplayOrder() : dto.getDisplayOrder());
        return quizQuestionRepository.save(question);
    }

    @Override
    public void deleteQuizQuestion(Long questionId, String teacherUsername) {
        QuizQuestion question = quizQuestionRepository.findById(questionId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Quiz question not found"));
        ensureOwnedByTeacher(question.getQuiz(), teacherUsername);
        quizQuestionRepository.delete(question);
    }

    @Override
    public List<Assignment> getAssignments(String teacherUsername) {
        Account teacher = getTeacherByUsername(teacherUsername);
        return assignmentRepository.findByTeacherIdOrderByIdDesc(teacher.getId());
    }

    @Override
    @Transactional
    public Assignment createAssignment(TeacherAssignmentRequestDTO dto, String teacherUsername) {
        Account teacher = getTeacherByUsername(teacherUsername);
        Quiz quiz = getOwnedQuiz(dto.getQuizId(), teacherUsername);
        Account student = accountRepository.findByIdAndRole(dto.getStudentId(), AccountRole.ROLE_STUDENT)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Student not found"));

        Assignment assignment = new Assignment();
        assignment.setTitle(dto.getTitle());
        assignment.setQuiz(quiz);
        assignment.setTeacher(teacher);
        assignment.setStudent(student);
        assignment.setDueAt(dto.getDueAt());
        assignment.setStatus(AssignmentStatus.PUBLISHED);
        return assignmentRepository.save(assignment);
    }

    @Override
    public Assignment updateAssignment(Long assignmentId, TeacherAssignmentRequestDTO dto, String teacherUsername) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Assignment not found"));

        Account teacher = getTeacherByUsername(teacherUsername);
        if (!assignment.getTeacher().getId().equals(teacher.getId())) {
            throw new CustomExceptions.BadRequestException("Cannot modify assignment not owned by teacher");
        }

        Quiz quiz = getOwnedQuiz(dto.getQuizId(), teacherUsername);
        Account student = accountRepository.findByIdAndRole(dto.getStudentId(), AccountRole.ROLE_STUDENT)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Student not found"));

        assignment.setTitle(dto.getTitle());
        assignment.setQuiz(quiz);
        assignment.setStudent(student);
        assignment.setDueAt(dto.getDueAt());
        return assignmentRepository.save(assignment);
    }

    @Override
    public void deleteAssignment(Long assignmentId, String teacherUsername) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Assignment not found"));

        Account teacher = getTeacherByUsername(teacherUsername);
        if (!assignment.getTeacher().getId().equals(teacher.getId())) {
            throw new CustomExceptions.BadRequestException("Cannot delete assignment not owned by teacher");
        }

        assignmentRepository.delete(assignment);
    }

    @Override
    public List<TeacherSubmissionDTO> getSubmissions(String teacherUsername) {
        Account teacher = getTeacherByUsername(teacherUsername);
        return quizAttemptRepository.findByQuizCreatedByIdOrderByStartedAtDesc(teacher.getId())
                .stream()
                .map(attempt -> new TeacherSubmissionDTO(
                        attempt.getId(),
                        attempt.getQuiz().getTitle(),
                        attempt.getStudent().getId(),
                        attempt.getStudent().getUsername(),
                        attempt.getScore(),
                        attempt.getStatus(),
                        attempt.getSubmittedAt()
                ))
                .toList();
    }

            @Override
            public List<TeacherClassInfoDTO> getAssignedClasses(String teacherUsername) {
            Account teacher = getTeacherByUsername(teacherUsername);

            return chemClassRepository.findByTeacherIdOrderByNameAsc(teacher.getId())
                .stream()
                .map(classRoom -> {
                    List<TeacherClassInfoDTO.StudentBrief> students = classStudentLinkRepository
                        .findByClassRoomIdOrderByStudentUsernameAsc(classRoom.getId())
                        .stream()
                        .map(link -> new TeacherClassInfoDTO.StudentBrief(
                            link.getStudent().getId(),
                            link.getStudent().getUsername(),
                            link.getStudent().getEmail(),
                            "/teacher/students/" + link.getStudent().getId()
                        ))
                        .toList();

                    return new TeacherClassInfoDTO(
                        classRoom.getId(),
                        classRoom.getName(),
                        classRoom.getSchedule(),
                        classRoom.getDescription(),
                        students
                    );
                })
                .toList();
            }

            @Override
            public TeacherStudentAccountDTO getStudentAccount(Long studentId, String teacherUsername) {
            Account teacher = getTeacherByUsername(teacherUsername);

            if (!classStudentLinkRepository.existsByStudentIdAndClassRoomTeacherId(studentId, teacher.getId())) {
                throw new CustomExceptions.BadRequestException("Student is not assigned to your classes");
            }

            Account student = accountRepository.findByIdAndRole(studentId, AccountRole.ROLE_STUDENT)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Student not found"));

            List<String> classes = classStudentLinkRepository.findByStudentIdAndClassRoomTeacherId(studentId, teacher.getId())
                .stream()
                .map(link -> link.getClassRoom().getName())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

            return new TeacherStudentAccountDTO(
                student.getId(),
                student.getUsername(),
                student.getEmail(),
                student.isEnabled(),
                classes
            );
            }

    @Override
    public List<TeacherStudentPerformanceDTO> getStudentPerformance(String teacherUsername) {
        Account teacher = getTeacherByUsername(teacherUsername);

        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizCreatedByIdOrderByStartedAtDesc(teacher.getId());
        List<Assignment> assignments = assignmentRepository.findByTeacherIdOrderByIdDesc(teacher.getId());

        Map<Long, List<QuizAttempt>> attemptsByStudent = attempts.stream()
                .collect(Collectors.groupingBy(a -> a.getStudent().getId()));

        Map<Long, List<Assignment>> assignmentByStudent = assignments.stream()
                .collect(Collectors.groupingBy(a -> a.getStudent().getId()));

        Set<Long> studentIds = new HashSet<>();
        studentIds.addAll(attemptsByStudent.keySet());
        studentIds.addAll(assignmentByStudent.keySet());

        List<TeacherStudentPerformanceDTO> result = new ArrayList<>();
        for (Long studentId : studentIds) {
            Account student = accountRepository.findById(studentId)
                    .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Student not found"));

            List<QuizAttempt> studentAttempts = attemptsByStudent.getOrDefault(studentId, List.of());
            int avgScore = studentAttempts.isEmpty()
                    ? 0
                    : (int) Math.round(studentAttempts.stream().mapToInt(QuizAttempt::getScore).average().orElse(0));

            List<Assignment> studentAssignments = assignmentByStudent.getOrDefault(studentId, List.of());
            long completedAssignments = studentAssignments.stream()
                    .filter(a -> a.getStatus() == AssignmentStatus.SUBMITTED || a.getStatus() == AssignmentStatus.REVIEWED)
                    .count();
            long pendingAssignments = studentAssignments.size() - completedAssignments;

            result.add(new TeacherStudentPerformanceDTO(
                    studentId,
                    student.getUsername(),
                    studentAttempts.size(),
                    avgScore,
                    completedAssignments,
                    pendingAssignments
            ));
        }

        result.sort(Comparator.comparing(TeacherStudentPerformanceDTO::getStudentName));
        return result;
    }

    @Override
    public TeacherDashboardSummaryDTO getSummary(String teacherUsername) {
        List<TeacherStudentPerformanceDTO> performance = getStudentPerformance(teacherUsername);
        int totalStudents = performance.size();
        int averageScore = performance.isEmpty()
                ? 0
                : (int) Math.round(performance.stream().mapToInt(TeacherStudentPerformanceDTO::getAverageScore).average().orElse(0));

        long totalLessons = lessonRepository.count();
        long totalQuizzes = getQuizzes(teacherUsername).size();
        long pendingAssignments = getAssignments(teacherUsername).stream()
                .filter(a -> a.getStatus() == AssignmentStatus.PUBLISHED || a.getStatus() == AssignmentStatus.OVERDUE)
                .count();

        return new TeacherDashboardSummaryDTO(totalStudents, averageScore, totalLessons, totalQuizzes, pendingAssignments);
    }

    private Account getTeacherByUsername(String teacherUsername) {
        Account teacher = accountRepository.findByUsername(teacherUsername)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Teacher account not found"));

        if (teacher.getRole() != AccountRole.ROLE_TEACHER) {
            throw new CustomExceptions.BadRequestException("Account is not a teacher");
        }

        return teacher;
    }

    private Quiz getOwnedQuiz(Long quizId, String teacherUsername) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Quiz not found"));
        ensureOwnedByTeacher(quiz, teacherUsername);
        return quiz;
    }

    private void ensureOwnedByTeacher(Quiz quiz, String teacherUsername) {
        Account teacher = getTeacherByUsername(teacherUsername);
        if (quiz.getCreatedBy() == null || !quiz.getCreatedBy().getId().equals(teacher.getId())) {
            throw new CustomExceptions.BadRequestException("Cannot access quiz not owned by teacher");
        }
    }

    private TeacherQuestionBankItemDTO toQuestionBankItemDto(QuestionBankItem item) {
        return new TeacherQuestionBankItemDTO(
                item.getId(),
                item.getPrompt(),
                item.getOptionA(),
                item.getOptionB(),
                item.getOptionC(),
                item.getOptionD(),
                item.getCorrectOption(),
                item.getExplanation(),
                item.getCreatedAt()
        );
    }
}
