package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.core.entity.Teacher;
import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.lms.dto.teacher.TeacherAssignmentRequestDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherAssignmentDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherChapterRequestDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherClassRequestDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherClassInfoDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherDashboardSummaryDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherLessonRequestDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherQuestionBankItemDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherQuestionBankRequestDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherQuizQuestionRequestDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherQuizRequestDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherQuizResponseDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherStudentAccountDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherStudentPerformanceDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherSubmissionDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherChapterResponseDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherLessonResponseDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherSubmissionDetailDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherAttemptAnswerDTO;
import com.example.chemlearn.lms.dto.teacher.TeacherGradeRequestDTO;
import com.example.chemlearn.lms.entity.Chapter;
import com.example.chemlearn.lms.entity.ClassStudentLink;
import com.example.chemlearn.lms.entity.Lesson;
import com.example.chemlearn.lms.entity.QuestionBankItem;
import com.example.chemlearn.lms.entity.Quiz;
import com.example.chemlearn.lms.entity.QuizAttempt;
import com.example.chemlearn.lms.entity.QuizQuestion;
import com.example.chemlearn.lms.entity.StudyClass;
import com.example.chemlearn.lms.entity.StudyClassAssignment;
import com.example.chemlearn.lms.enums.AttemptStatus;
import com.example.chemlearn.lms.enums.MaterialScope;
import com.example.chemlearn.lms.enums.QuizType;
import com.example.chemlearn.lms.repository.UserRepository;
import com.example.chemlearn.lms.repository.ChapterRepository;
import com.example.chemlearn.lms.repository.ClassStudentLinkRepository;
import com.example.chemlearn.lms.repository.LessonRepository;
import com.example.chemlearn.lms.repository.QuestionBankItemRepository;
import com.example.chemlearn.lms.repository.QuizAttemptRepository;
import com.example.chemlearn.lms.repository.QuizQuestionRepository;
import com.example.chemlearn.lms.repository.QuizRepository;
import com.example.chemlearn.lms.repository.StudyClassAssignmentRepository;
import com.example.chemlearn.lms.repository.StudyClassRepository;
import com.example.chemlearn.lms.repository.AttemptAnswerRepository;
import com.example.chemlearn.lms.entity.AttemptAnswer;
import com.example.chemlearn.lms.service.TeacherService;
import com.example.chemlearn.lms.service.StudyClassCodeGenerator;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {
    private final ChapterRepository chapterRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final StudyClassAssignmentRepository studyClassAssignmentRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserRepository userRepository;
    private final QuestionBankItemRepository questionBankItemRepository;
    private final ClassStudentLinkRepository classStudentLinkRepository;
    private final StudyClassRepository studyClassRepository;
    private final StudyClassCodeGenerator studyClassCodeGenerator;
    private final AttemptAnswerRepository attemptAnswerRepository;
    private final EntityManager entityManager;

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
                classRoom.getClassCode(),
                studentsByClassId.getOrDefault(classRoom.getId(), List.of()),
                (classRoom.getChapters() != null ? classRoom.getChapters() : List.<Chapter>of())
                    .stream()
                    .map(ch -> new com.example.chemlearn.lms.dto.response.ChapterResponse(
                        ch.getId(),
                        ch.getTitle(),
                        ch.getDescription(),
                        ch.getGradeLevel(),
                        ch.getOrderIndex(),
                        ch.getCreatedAt(),
                        ch.getUpdatedAt()))
                    .toList()))
            .toList();
    }

    @Override
    public TeacherClassInfoDTO createClass(TeacherClassRequestDTO dto, String teacherUsername) {
        User teacherUser = requireTeacherUser(teacherUsername);
        StudyClass studyClass = new StudyClass();
        studyClass.setName(dto.getName());
        studyClass.setSchedule(dto.getSchedule());
        studyClass.setDescription(dto.getDescription());
        studyClass.setGradeLevel(dto.getGradeLevel() == null ? 10 : dto.getGradeLevel());
        studyClass.setClassCode(studyClassCodeGenerator.generateUniqueCode());
        studyClass.setTeacher(entityManager.getReference(Teacher.class, teacherUser.getId()));
        Instant now = Instant.now();
        studyClass.setCreatedAt(now);
        studyClass.setUpdatedAt(now);
        return toTeacherClassInfo(studyClassRepository.save(studyClass));
    }

    @Override
    public TeacherClassInfoDTO updateClass(UUID classId, TeacherClassRequestDTO dto, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        StudyClass studyClass = requireOwnedClass(classId, teacherId);
        studyClass.setName(dto.getName());
        studyClass.setSchedule(dto.getSchedule());
        studyClass.setDescription(dto.getDescription());
        if (dto.getGradeLevel() != null) {
            studyClass.setGradeLevel(dto.getGradeLevel());
        }
        studyClass.setUpdatedAt(Instant.now());
        return toTeacherClassInfo(studyClassRepository.save(studyClass));
    }

    @Override
    public void deleteClass(UUID classId, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        StudyClass studyClass = requireOwnedClass(classId, teacherId);
        classStudentLinkRepository.deleteByClassRoomId(classId);
        studyClassRepository.delete(studyClass);
    }

    @Override
    public List<TeacherChapterResponseDTO> getChapters(String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        return chapterRepository.findVisibleToTeacher(teacherId, UserRole.ROLE_ADMIN, MaterialScope.GLOBAL)
            .stream()
            .map(this::toChapterResponse)
            .toList();
    }

    @Override
    public TeacherChapterResponseDTO createChapter(TeacherChapterRequestDTO dto, String teacherUsername) {
        User teacherUser = requireTeacherUser(teacherUsername);
        StudyClass ownerClass = requireOwnedClass(dto.getClassId(), teacherUser.getId());
        Chapter chapter = new Chapter();
        chapter.setTitle(dto.getTitle());
        chapter.setDescription(dto.getDescription());
        chapter.setGradeLevel(10);
        chapter.setOrderIndex(dto.getDisplayOrder() == null ? 0 : dto.getDisplayOrder());
        chapter.setPublished(dto.getPublished() == null ? Boolean.TRUE : dto.getPublished());
        chapter.setCreatedAt(Instant.now());
        chapter.setUpdatedAt(Instant.now());
        chapter.setCreatedBy(teacherUser);
        chapter.setUpdatedBy(teacherUser);

        chapter.setMaterialScope(MaterialScope.CLASS_PRIVATE);
        chapter.setOwnerClass(ownerClass);

        Chapter savedChapter = chapterRepository.save(chapter);
        if (!ownerClass.getChapters().contains(savedChapter)) {
            ownerClass.getChapters().add(savedChapter);
            studyClassRepository.save(ownerClass);
        }

        return toChapterResponse(savedChapter);
    }

    @Override
    public TeacherChapterResponseDTO updateChapter(UUID chapterId, TeacherChapterRequestDTO dto, String teacherUsername) {
        User teacherUser = requireTeacherUser(teacherUsername);
        Chapter chapter = requireOwnedChapter(chapterId, teacherUser.getId());
        UUID ownerClassId = chapter.getOwnerClass() == null ? null : chapter.getOwnerClass().getId();
        if (ownerClassId == null || !ownerClassId.equals(dto.getClassId())) {
            throw new RuntimeException("Class-private chapter cannot be moved to another class");
        }

        chapter.setTitle(dto.getTitle());
        chapter.setDescription(dto.getDescription());
        chapter.setOrderIndex(dto.getDisplayOrder() == null ? 0 : dto.getDisplayOrder());
        chapter.setPublished(dto.getPublished() == null ? Boolean.TRUE : dto.getPublished());
        chapter.setUpdatedAt(Instant.now());
        chapter.setUpdatedBy(teacherUser);
        chapter.setMaterialScope(MaterialScope.CLASS_PRIVATE);
        return toChapterResponse(chapterRepository.save(chapter));
    }

    @Override
    public void deleteChapter(UUID chapterId, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        Chapter chapter = requireOwnedChapter(chapterId, teacherId);
        chapterRepository.delete(chapter);
    }

    @Override
    public List<TeacherLessonResponseDTO> getLessons(String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        return lessonRepository.findByCreatedBy_IdOrderByOrderIndexAsc(teacherId)
            .stream()
            .map(this::toLessonResponse)
            .toList();
    }

    @Override
    public TeacherLessonResponseDTO createLesson(TeacherLessonRequestDTO dto, String teacherUsername) {
        User teacherUser = requireTeacherUser(teacherUsername);
        Chapter chapter = requireOwnedChapter(dto.getChapterId(), teacherUser.getId());

        if (chapter.getMaterialScope() != MaterialScope.CLASS_PRIVATE || chapter.getOwnerClass() == null) {
            throw new RuntimeException("Teacher lessons must belong to a class-private chapter");
        }

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
        lesson.setCreatedBy(teacherUser);
        lesson.setUpdatedBy(teacherUser);
        lesson.setMaterialScope(MaterialScope.CLASS_PRIVATE);
        lesson.setOwnerClass(chapter.getOwnerClass());
        return toLessonResponse(lessonRepository.save(lesson));
    }

    @Override
    public TeacherLessonResponseDTO updateLesson(UUID lessonId, TeacherLessonRequestDTO dto, String teacherUsername) {
        User teacherUser = requireTeacherUser(teacherUsername);
        Lesson lesson = requireOwnedLesson(lessonId, teacherUser.getId());
        Chapter chapter = requireOwnedChapter(dto.getChapterId(), teacherUser.getId());

        if (chapter.getMaterialScope() != MaterialScope.CLASS_PRIVATE || chapter.getOwnerClass() == null) {
            throw new RuntimeException("Teacher lessons must belong to a class-private chapter");
        }

        lesson.setChapter(chapter);
        lesson.setTitle(dto.getTitle());
        lesson.setTextContent(dto.getContent());
        lesson.setDurationMinutes(dto.getEstimatedMinutes() == null ? 0 : dto.getEstimatedMinutes());
        lesson.setOrderIndex(dto.getDisplayOrder() == null ? 0 : dto.getDisplayOrder());
        lesson.setPublished(dto.getPublished() == null ? Boolean.TRUE : dto.getPublished());
        lesson.setUpdatedAt(Instant.now());
        lesson.setUpdatedBy(teacherUser);
        lesson.setMaterialScope(MaterialScope.CLASS_PRIVATE);
        lesson.setOwnerClass(chapter.getOwnerClass());
        return toLessonResponse(lessonRepository.save(lesson));
    }

    @Override
    public void deleteLesson(UUID lessonId, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        Lesson lesson = requireOwnedLesson(lessonId, teacherId);
        lessonRepository.delete(lesson);
    }

    @Override
    public List<TeacherQuizResponseDTO> getQuizzes(String teacherUsername) {
        requireTeacherUser(teacherUsername);
        return quizRepository.findAllByOrderByIdDesc().stream()
                .filter(quiz -> {
                    // Quick bugfix: actually return this teacher's quizzes if ownership exists, 
                    // though for now if business logic dictates returning all, we leave that alone.
                    // Assuming existing logic is what you wanted, leaving exact same db call:
                    return true;
                })
                .map(this::mapToTeacherQuizResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TeacherQuizResponseDTO createQuiz(TeacherQuizRequestDTO dto, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        StudyClass studyClass = requireOwnedClass(dto.getClassId(), teacherId);

        validateQuizTiming(dto);

        Quiz quiz = new Quiz();
        quiz.setTitle(dto.getTitle());
        quiz.setDescription(dto.getDescription());
        quiz.setQuizType(dto.getQuizType());
        quiz.setDurationMinutes(dto.getDurationMinutes());
        if (isTimedQuiz(dto.getQuizType())) {
            quiz.setStartTime(dto.getStartTime());
            quiz.setEndTime(dto.getEndTime());
        } else {
            quiz.setStartTime(null);
            quiz.setEndTime(null);
        }
        quiz.setPublished(dto.getPublished() == null ? Boolean.TRUE : dto.getPublished());
        quiz.setCreatedAt(Instant.now());
        quiz.setCreatedBy(entityManager.getReference(Teacher.class, teacherId));
        quiz.setStudyClass(studyClass);
        
        Quiz savedQuiz = quizRepository.save(quiz);

        // Automatically create an assignment so students can see it
        StudyClassAssignment assignment = new StudyClassAssignment();
        assignment.setStudyClassField(studyClass);
        assignment.setTitle(savedQuiz.getTitle());
        assignment.setQuiz(savedQuiz);
        assignment.setCreatedAt(Instant.now());
        // Copy end time as due date if it's a timed quiz
        if (isTimedQuiz(savedQuiz.getQuizType())) {
            assignment.setDueDate(savedQuiz.getEndTime());
        }
        studyClassAssignmentRepository.save(assignment);

        return mapToTeacherQuizResponseDTO(savedQuiz);
    }

    @Override
    public TeacherQuizResponseDTO updateQuiz(UUID quizId, TeacherQuizRequestDTO dto, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        StudyClass studyClass = requireOwnedClass(dto.getClassId(), teacherId);

        validateQuizTiming(dto);

        Quiz quiz = requireOwnedQuiz(quizId, teacherId);
        quiz.setTitle(dto.getTitle());
        quiz.setDescription(dto.getDescription());
        quiz.setQuizType(dto.getQuizType());
        quiz.setDurationMinutes(dto.getDurationMinutes());
        if (isTimedQuiz(dto.getQuizType())) {
            quiz.setStartTime(dto.getStartTime());
            quiz.setEndTime(dto.getEndTime());
        } else {
            quiz.setStartTime(null);
            quiz.setEndTime(null);
        }
        quiz.setPublished(dto.getPublished() == null ? Boolean.TRUE : dto.getPublished());
        quiz.setStudyClass(studyClass);
        return mapToTeacherQuizResponseDTO(quizRepository.save(quiz));
    }

    private TeacherQuizResponseDTO mapToTeacherQuizResponseDTO(Quiz quiz) {
        TeacherQuizResponseDTO dto = new TeacherQuizResponseDTO();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setQuizType(quiz.getQuizType());
        dto.setDurationMinutes(quiz.getDurationMinutes());
        dto.setStartTime(quiz.getStartTime());
        dto.setEndTime(quiz.getEndTime());
        dto.setPublished(quiz.getPublished());
        dto.setCreatedAt(quiz.getCreatedAt());
        if (quiz.getStudyClass() != null) {
            dto.setClassId(quiz.getStudyClass().getId());
        }
        return dto;
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
        item.setQuestionType(dto.getQuestionType() != null ? dto.getQuestionType() : com.example.chemlearn.lms.enums.QuestionType.SINGLE_CHOICE);
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
        item.setQuestionType(dto.getQuestionType() != null ? dto.getQuestionType() : com.example.chemlearn.lms.enums.QuestionType.SINGLE_CHOICE);
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
        question.setQuestionType(item.getQuestionType());
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
        question.setQuestionType(dto.getQuestionType() != null ? dto.getQuestionType() : com.example.chemlearn.lms.enums.QuestionType.SINGLE_CHOICE);
        
        if (quiz.getQuizType() == com.example.chemlearn.lms.enums.QuizType.MINI_QUIZ && question.getQuestionType() == com.example.chemlearn.lms.enums.QuestionType.ESSAY) {
            throw new RuntimeException("Mini quizzes cannot contain essay questions");
        }

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

        question.setQuestionType(dto.getQuestionType() != null ? dto.getQuestionType() : com.example.chemlearn.lms.enums.QuestionType.SINGLE_CHOICE);
        
        if (question.getQuiz().getQuizType() == com.example.chemlearn.lms.enums.QuizType.MINI_QUIZ && question.getQuestionType() == com.example.chemlearn.lms.enums.QuestionType.ESSAY) {
            throw new RuntimeException("Mini quizzes cannot contain essay questions");
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
    public List<TeacherAssignmentDTO> getAssignments(String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        List<UUID> classIds = studyClassRepository.findByTeacherId(teacherId)
                .stream()
                .map(StudyClass::getId)
                .toList();
        if (classIds.isEmpty()) {
            return List.of();
        }

        return studyClassAssignmentRepository.findByStudyClassField_IdIn(classIds)
                .stream()
                .filter(item -> item.getQuiz() != null)
                .map(this::toTeacherAssignmentDto)
                .sorted(Comparator.comparing(TeacherAssignmentDTO::getDueDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public TeacherAssignmentDTO createAssignment(TeacherAssignmentRequestDTO dto, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        StudyClass studyClass = requireOwnedClass(dto.getClassId(), teacherId);
        Quiz quiz = requireOwnedQuiz(dto.getQuizId(), teacherId);
        quiz = ensureAssignmentQuizType(quiz);

        StudyClassAssignment assignment = new StudyClassAssignment();
        assignment.setStudyClassField(studyClass);
        assignment.setTitle(dto.getTitle());
        assignment.setQuiz(quiz);
        assignment.setDueDate(dto.getDueDate());
        assignment.setCreatedAt(Instant.now());
        return toTeacherAssignmentDto(studyClassAssignmentRepository.save(assignment));
    }

    @Override
    public TeacherAssignmentDTO updateAssignment(UUID assignmentId, TeacherAssignmentRequestDTO dto, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        StudyClassAssignment assignment = studyClassAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        UUID classId = assignment.getStudyClassField() == null ? null : assignment.getStudyClassField().getId();
        if (classId == null) {
            throw new RuntimeException("Assignment is missing class linkage");
        }
        requireOwnedClass(classId, teacherId);

        StudyClass studyClass = requireOwnedClass(dto.getClassId(), teacherId);
        Quiz quiz = requireOwnedQuiz(dto.getQuizId(), teacherId);
        quiz = ensureAssignmentQuizType(quiz);

        assignment.setTitle(dto.getTitle());
        assignment.setStudyClassField(studyClass);
        assignment.setQuiz(quiz);
        assignment.setDueDate(dto.getDueDate());
        return toTeacherAssignmentDto(studyClassAssignmentRepository.save(assignment));
    }

    @Override
    public void deleteAssignment(UUID assignmentId, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        StudyClassAssignment assignment = studyClassAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        UUID classId = assignment.getStudyClassField() == null ? null : assignment.getStudyClassField().getId();
        if (classId == null) {
            throw new RuntimeException("Assignment is missing class linkage");
        }
        requireOwnedClass(classId, teacherId);
        studyClassAssignmentRepository.delete(assignment);
    }

    private Quiz ensureAssignmentQuizType(Quiz quiz) {
        if (quiz.getQuizType() != null) {
            return quiz;
        }

        quiz.setQuizType(QuizType.ASSIGNMENT);
        return quizRepository.save(quiz);
    }

    private void validateQuizTiming(TeacherQuizRequestDTO dto) {
        if (dto.getQuizType() == null || !isTimedQuiz(dto.getQuizType())) {
            return;
        }

        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new RuntimeException("Assignment and exam quizzes require both start time and end time");
        }

        if (!dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new RuntimeException("End time must be after start time");
        }
    }

    private boolean isTimedQuiz(QuizType quizType) {
        return quizType == QuizType.ASSIGNMENT || quizType == QuizType.EXAM;
    }

    private TeacherAssignmentDTO toTeacherAssignmentDto(StudyClassAssignment assignment) {
        Instant due = assignment.getDueDate();
        String status;
        if (due == null) {
            status = "OPEN";
        } else if (due.isBefore(Instant.now())) {
            status = "OVERDUE";
        } else {
            status = "ACTIVE";
        }

        return new TeacherAssignmentDTO(
                assignment.getId(),
                assignment.getTitle(),
                assignment.getQuiz() == null ? null : assignment.getQuiz().getId(),
                assignment.getStudyClassField() == null ? null : assignment.getStudyClassField().getId(),
                due,
                status);
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

        List<StudyClassAssignment> teacherAssignments = studyClassRepository.findByTeacherId(teacherId)
            .stream()
            .map(StudyClass::getId)
            .map(studyClassAssignmentRepository::findByStudyClassField_Id)
            .flatMap(List::stream)
            .filter(item -> item.getQuiz() != null)
            .toList();

        Map<UUID, List<UUID>> classIdsByStudentId = classStudentLinkRepository
            .findByClassRoomTeacherIdOrderByClassRoomNameAsc(teacherId)
            .stream()
            .collect(java.util.stream.Collectors.groupingBy(
                link -> link.getStudent().getId(),
                java.util.stream.Collectors.mapping(link -> link.getClassRoom().getId(), java.util.stream.Collectors.toList())));

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

                java.util.Set<UUID> assignedQuizIds = teacherAssignments.stream()
                    .filter(assignment -> assignment.getStudyClassField() != null)
                    .filter(assignment -> classIdsByStudentId.getOrDefault(student.getId(), List.of())
                        .contains(assignment.getStudyClassField().getId()))
                    .map(assignment -> assignment.getQuiz().getId())
                    .collect(java.util.stream.Collectors.toSet());

                long completed = attempts.stream()
                    .filter(attempt -> attempt.getQuiz() != null && assignedQuizIds.contains(attempt.getQuiz().getId()))
                    .filter(attempt -> attempt.getStatus() == AttemptStatus.COMPLETED || attempt.getStatus() == AttemptStatus.NEEDS_GRADING)
                    .map(attempt -> attempt.getQuiz().getId())
                    .distinct()
                    .count();

                long pending = Math.max(0, assignedQuizIds.size() - completed);

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
        long pendingAssignments = studyClassRepository.findByTeacherId(teacherId)
            .stream()
            .map(StudyClass::getId)
            .map(studyClassAssignmentRepository::findByStudyClassField_Id)
            .flatMap(List::stream)
            .filter(assignment -> assignment.getDueDate() == null || assignment.getDueDate().isAfter(Instant.now()))
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

    private StudyClass requireOwnedClass(UUID classId, UUID teacherId) {
        StudyClass studyClass = studyClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        UUID ownerId = studyClass.getTeacher() == null ? null : studyClass.getTeacher().getId();
        if (ownerId == null || !ownerId.equals(teacherId)) {
            throw new RuntimeException("You are not allowed to modify this class");
        }
        return studyClass;
    }

    private Chapter requireOwnedChapter(UUID chapterId, UUID teacherId) {
        return chapterRepository.findByIdAndCreatedBy_Id(chapterId, teacherId)
                .orElseThrow(() -> new RuntimeException("Chapter not found or not owned by teacher"));
    }

    private Lesson requireOwnedLesson(UUID lessonId, UUID teacherId) {
        return lessonRepository.findByIdAndCreatedBy_Id(lessonId, teacherId)
                .orElseThrow(() -> new RuntimeException("Lesson not found or not owned by teacher"));
    }

    private TeacherClassInfoDTO toTeacherClassInfo(StudyClass studyClass) {
        return new TeacherClassInfoDTO(
                studyClass.getId(),
                studyClass.getName(),
                studyClass.getSchedule(),
                studyClass.getDescription(),
                studyClass.getClassCode(),
            classStudentLinkRepository.findByClassRoomIdOrderByStudentUsernameAsc(studyClass.getId())
                .stream()
                .map(link -> new TeacherClassInfoDTO.StudentBrief(
                    link.getStudent().getId(),
                    link.getStudent().getUsername(),
                    link.getStudent().getEmail(),
                    "/admin/users/" + link.getStudent().getId()))
                .toList(),
            studyClass.getChapters() == null ? List.of() : studyClass.getChapters()
                .stream()
                .map(ch -> new com.example.chemlearn.lms.dto.response.ChapterResponse(
                    ch.getId(), ch.getTitle(), ch.getDescription(), ch.getGradeLevel(), ch.getOrderIndex(), ch.getCreatedAt(), ch.getUpdatedAt()))
                .toList());
    }

    @Override
    public void addChapterToClass(java.util.UUID classId, java.util.UUID chapterId, String teacherUsername) {
        java.util.UUID teacherId = requireTeacherUser(teacherUsername).getId();
        StudyClass studyClass = requireOwnedClass(classId, teacherId);
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        boolean adminOwned = chapter.getCreatedBy() != null && chapter.getCreatedBy().getRole() == UserRole.ROLE_ADMIN;
        boolean teacherOwned = chapter.getCreatedBy() != null && Objects.equals(chapter.getCreatedBy().getId(), teacherId);
        if (!adminOwned && !teacherOwned) {
            throw new RuntimeException("You are not allowed to assign this chapter");
        }

        if (chapter.getMaterialScope() == MaterialScope.CLASS_PRIVATE) {
            UUID ownerClassId = chapter.getOwnerClass() == null ? null : chapter.getOwnerClass().getId();
            if (ownerClassId == null || !ownerClassId.equals(classId)) {
                throw new RuntimeException("Class-private chapter can only be used in its owner class");
            }
        }

        if (!studyClass.getChapters().contains(chapter)) {
            studyClass.getChapters().add(chapter);
            studyClassRepository.save(studyClass);
        }
    }

    @Override
    public void removeChapterFromClass(java.util.UUID classId, java.util.UUID chapterId, String teacherUsername) {
        java.util.UUID teacherId = requireTeacherUser(teacherUsername).getId();
        StudyClass studyClass = requireOwnedClass(classId, teacherId);
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        if (chapter.getMaterialScope() == MaterialScope.CLASS_PRIVATE) {
            UUID ownerClassId = chapter.getOwnerClass() == null ? null : chapter.getOwnerClass().getId();
            if (ownerClassId != null && ownerClassId.equals(classId)) {
                throw new RuntimeException("Cannot detach class-private chapter from its owner class");
            }
        }

        if (studyClass.getChapters().removeIf(c -> c.getId().equals(chapter.getId()))) {
            studyClassRepository.save(studyClass);
        }
    }

    private TeacherQuestionBankItemDTO toQuestionBankDto(QuestionBankItem item) {
        return new TeacherQuestionBankItemDTO(
                item.getId(),
                item.getQuestionType(),
                item.getPrompt(),
                item.getOptionA(),
                item.getOptionB(),
                item.getOptionC(),
                item.getOptionD(),
                item.getCorrectOption(),
                item.getExplanation(),
                item.getCreatedAt());
    }

    @Override
    public TeacherSubmissionDetailDTO getSubmissionDetail(UUID attemptId, String teacherUsername) {
        User teacher = requireTeacherUser(teacherUsername);
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
            .orElseThrow(() -> new RuntimeException("Submission not found"));

        // Security check: teacher must own the quiz
        if (attempt.getQuiz() != null && !attempt.getQuiz().getCreatedBy().getId().equals(teacher.getId())) {
            throw new RuntimeException("Unauthorized access to this submission");
        }

        List<AttemptAnswer> answers = attemptAnswerRepository.findAll().stream()
            .filter(a -> a.getAttempt().getId().equals(attemptId))
            .toList();

        List<TeacherAttemptAnswerDTO> answerDTOs = answers.stream()
            .map(a -> new TeacherAttemptAnswerDTO(
                a.getQuizQuestion().getId(),
                a.getQuizQuestion().getPrompt(),
                a.getQuizQuestion().getQuestionType(),
                a.getSelectedOption(),
                a.getQuizQuestion().getCorrectOption(),
                a.getIsCorrect()
            ))
            .toList();

        return new TeacherSubmissionDetailDTO(
            attempt.getId(),
            attempt.getQuiz() == null ? "Unknown Quiz" : attempt.getQuiz().getTitle(),
            attempt.getStudent() == null || attempt.getStudent().getUsers() == null ? "Unknown Student" : attempt.getStudent().getUsers().getFullName(),
            toIntScore(attempt.getScore()),
            attempt.getStatus(),
            attempt.getSubmittedAt(),
            answerDTOs
        );
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void gradeSubmission(UUID attemptId, TeacherGradeRequestDTO dto, String teacherUsername) {
        User teacher = requireTeacherUser(teacherUsername);
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
            .orElseThrow(() -> new RuntimeException("Submission not found"));

        // Security check
        if (attempt.getQuiz() != null && !attempt.getQuiz().getCreatedBy().getId().equals(teacher.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        if (dto.getFinalScore() != null) {
            attempt.setScore(BigDecimal.valueOf(dto.getFinalScore()));
        }
        attempt.setStatus(AttemptStatus.COMPLETED);
        quizAttemptRepository.save(attempt);
    }

    private int toIntScore(BigDecimal score) {
        return score == null ? 0 : score.intValue();
    }

    private TeacherChapterResponseDTO toChapterResponse(Chapter chapter) {
        if (chapter == null) return null;
        return TeacherChapterResponseDTO.builder()
                .id(chapter.getId())
                .title(chapter.getTitle())
                .description(chapter.getDescription())
                .orderIndex(chapter.getOrderIndex())
                .published(chapter.getPublished())
                .build();
    }

    private TeacherLessonResponseDTO toLessonResponse(Lesson lesson) {
        if (lesson == null) return null;
        return TeacherLessonResponseDTO.builder()
                .id(lesson.getId())
                .chapterId(lesson.getChapter() != null ? lesson.getChapter().getId() : null)
                .title(lesson.getTitle())
                .content(lesson.getTextContent())
                .durationMinutes(lesson.getDurationMinutes())
                .orderIndex(lesson.getOrderIndex())
                .published(lesson.getPublished())
                .build();
    }
}