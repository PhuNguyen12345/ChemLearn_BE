package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.lms.dto.quiz.QuizListItemDTO;
import com.example.chemlearn.lms.dto.response.ChapterResponse;
import com.example.chemlearn.lms.dto.response.StudyClassAssignmentResponse;
import com.example.chemlearn.lms.dto.response.StudyClassResponse;
import com.example.chemlearn.lms.dto.study.LessonDetailDTO;
import com.example.chemlearn.lms.dto.study.LessonSummaryDTO;
import com.example.chemlearn.lms.dto.study.MiniQuizQuestionDTO;
import com.example.chemlearn.lms.entity.ClassStudentLink;
import com.example.chemlearn.lms.entity.Chapter;
import com.example.chemlearn.lms.entity.Lesson;
import com.example.chemlearn.lms.entity.MiniQuizQuestion;
import com.example.chemlearn.lms.entity.Quiz;
import com.example.chemlearn.lms.entity.StudyClassAssignment;
import com.example.chemlearn.lms.entity.StudyClass;
import com.example.chemlearn.lms.enums.MaterialScope;
import com.example.chemlearn.lms.enums.QuestionType;
import com.example.chemlearn.lms.repository.ClassStudentLinkRepository;
import com.example.chemlearn.lms.repository.LessonRepository;
import com.example.chemlearn.lms.repository.MiniQuizQuestionRepository;
import com.example.chemlearn.lms.repository.QuizQuestionRepository;
import com.example.chemlearn.lms.repository.StudyClassAssignmentRepository;
import com.example.chemlearn.lms.repository.StudyClassRepository;
import com.example.chemlearn.lms.repository.UserRepository;
import com.example.chemlearn.lms.service.StudentClassService;
import java.time.Instant;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentClassServiceImpl implements StudentClassService {
    private final UserRepository userRepository;
    private final StudyClassRepository studyClassRepository;
    private final ClassStudentLinkRepository classStudentLinkRepository;
    private final StudyClassAssignmentRepository assignmentRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final LessonRepository lessonRepository;
    private final MiniQuizQuestionRepository miniQuizQuestionRepository;

    @Override
    public List<StudyClassResponse> getMyClasses(String studentUsername) {
        User studentUser = requireStudentUser(studentUsername);
        return classStudentLinkRepository.findByStudentId(studentUser.getId())
                .stream()
                .map(ClassStudentLink::getClassRoom)
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<QuizListItemDTO> getMyQuizzes(String studentUsername) {
        User studentUser = requireStudentUser(studentUsername);
        List<UUID> classIds = classStudentLinkRepository.findByStudentId(studentUser.getId())
            .stream()
            .map(link -> link.getClassRoom().getId())
            .toList();
        if (classIds.isEmpty()) {
            return List.of();
        }

        List<StudyClassAssignment> assignments = assignmentRepository.findByStudyClassField_IdIn(classIds);
        return mapAssignmentsToQuizList(assignments);
    }

    @Override
    public List<QuizListItemDTO> getQuizzesForClass(String studentUsername, UUID classId) {
        requireEnrolledClass(studentUsername, classId);
        List<StudyClassAssignment> assignments = assignmentRepository.findByStudyClassField_Id(classId);
        return mapAssignmentsToQuizList(assignments);
    }

        @Override
        public List<StudyClassAssignmentResponse> getMyAssignments(String studentUsername) {
        User studentUser = requireStudentUser(studentUsername);
        List<UUID> classIds = classStudentLinkRepository.findByStudentId(studentUser.getId())
            .stream()
            .map(link -> link.getClassRoom().getId())
            .toList();
        if (classIds.isEmpty()) {
            return List.of();
        }

        return assignmentRepository.findByStudyClassField_IdIn(classIds)
            .stream()
            .map(this::toAssignmentResponse)
            .toList();
        }

    @Override
    public List<StudyClassAssignmentResponse> getAssignmentsForClass(String studentUsername, UUID classId) {
        requireEnrolledClass(studentUsername, classId);
        return assignmentRepository.findByStudyClassField_Id(classId)
            .stream()
            .map(this::toAssignmentResponse)
            .toList();
    }

    @Override
    public StudyClassResponse joinClassByCode(String studentUsername, String classCode) {
        User studentUser = requireStudentUser(studentUsername);
        StudyClass studyClass = studyClassRepository.findByClassCode(normalizeClassCode(classCode))
                .orElseThrow(() -> new RuntimeException("Class code not found"));

        boolean alreadyJoined = classStudentLinkRepository.findByStudentId(studentUser.getId())
                .stream()
                .anyMatch(link -> link.getClassRoom() != null && link.getClassRoom().getId().equals(studyClass.getId()));
        if (alreadyJoined) {
            return toResponse(studyClass);
        }

        ClassStudentLink link = new ClassStudentLink();
        link.setClassRoom(studyClass);
        link.setStudent(studentUser);
        classStudentLinkRepository.save(link);
        return toResponse(studyClass);
    }

    @Override
    public void leaveClass(String studentUsername, UUID classId) {
        User studentUser = requireStudentUser(studentUsername);
        ClassStudentLink enrollment = classStudentLinkRepository.findByStudentIdAndClassRoomId(studentUser.getId(), classId)
            .orElseThrow(() -> new RuntimeException("You are not enrolled in this class"));
        classStudentLinkRepository.delete(enrollment);
    }

    private User requireStudentUser(String studentUsername) {
        User user = userRepository.findByUsername(studentUsername)
                .orElseThrow(() -> new RuntimeException("Student account not found"));
        if (user.getRole() != UserRole.ROLE_STUDENT) {
            throw new RuntimeException("Current account is not a student");
        }
        return user;
    }

    private String normalizeClassCode(String classCode) {
        if (classCode == null) {
            throw new RuntimeException("Class code is required");
        }
        String normalized = classCode.replaceAll("\\s+", "").trim();
        if (!normalized.matches("\\d{6}")) {
            throw new RuntimeException("Class code must be a 6-digit number");
        }
        return normalized;
    }

    private StudyClassResponse toResponse(StudyClass studyClass) {
        return new StudyClassResponse(
                studyClass.getId(),
                studyClass.getName(),
                studyClass.getClassCode(),
                studyClass.getGradeLevel(),
                studyClass.getTeacher() == null ? null : studyClass.getTeacher().getId(),
                studyClass.getCreatedAt(),
                studyClass.getUpdatedAt());
    }

    private StudyClassAssignmentResponse toAssignmentResponse(StudyClassAssignment assignment) {
        return new StudyClassAssignmentResponse(
                assignment.getId(),
                assignment.getStudyClassField() == null ? null : assignment.getStudyClassField().getId(),
                assignment.getTitle(),
                assignment.getLab() == null ? null : assignment.getLab().getId(),
                assignment.getQuiz() == null ? null : assignment.getQuiz().getId(),
                assignment.getDueDate(),
                assignment.getCreatedAt());
    }

    private List<QuizListItemDTO> mapAssignmentsToQuizList(List<StudyClassAssignment> assignments) {
        if (assignments == null || assignments.isEmpty()) {
            return List.of();
        }

        Map<UUID, StudyClassAssignment> latestByQuizId = assignments.stream()
            .filter(assignment -> assignment != null
                && assignment.getQuiz() != null
                && Boolean.TRUE.equals(assignment.getQuiz().getPublished()))
            .collect(java.util.stream.Collectors.toMap(
                assignment -> assignment.getQuiz().getId(),
                java.util.function.Function.identity(),
                this::pickLatestAssignment,
                LinkedHashMap::new));

        return latestByQuizId.values().stream()
            .map(assignment -> {
                Quiz quiz = assignment.getQuiz();
                return new QuizListItemDTO(
                    quiz.getId(),
                    quiz.getTitle(),
                    quiz.getDescription(),
                    quiz.getQuizType(),
                    quiz.getDurationMinutes(),
                    Math.toIntExact(quizQuestionRepository.countByQuizId(quiz.getId())),
                    assignment.getDueDate());
            })
            .toList();
    }

    private StudyClassAssignment pickLatestAssignment(StudyClassAssignment left, StudyClassAssignment right) {
        Instant leftAt = left == null ? null : left.getCreatedAt();
        Instant rightAt = right == null ? null : right.getCreatedAt();
        if (leftAt == null) {
            return right;
        }
        if (rightAt == null) {
            return left;
        }
        return rightAt.isAfter(leftAt) ? right : left;
    }

    @Override
    public List<ChapterResponse> getChaptersForClass(String studentUsername, UUID classId) {
        StudyClass studyClass = requireEnrolledClass(studentUsername, classId);
        return studyClass.getChapters().stream()
            .filter(chapter -> chapter != null
                && Boolean.TRUE.equals(chapter.getPublished())
                && isChapterVisibleInClass(chapter, classId))
            .map(this::mapChapterToResponse)
            .toList();
    }

    @Override
    public List<LessonSummaryDTO> getLessonsForClassChapter(String studentUsername, UUID classId, UUID chapterId) {
        Chapter chapter = requireVisibleClassChapter(studentUsername, classId, chapterId);
        return lessonRepository.findByChapterIdAndPublishedTrueOrderByOrderIndexAsc(chapterId)
            .stream()
            .filter(lesson -> lesson != null && isLessonVisibleInClass(lesson, classId))
            .map(lesson -> new LessonSummaryDTO(lesson.getId(), lesson.getTitle(), lesson.getDurationMinutes()))
            .toList();
    }

    @Override
    public LessonDetailDTO getLessonDetailForClass(String studentUsername, UUID classId, UUID lessonId) {
        requireEnrolledClass(studentUsername, classId);
        Lesson lesson = lessonRepository.findByIdAndPublishedTrue(lessonId)
            .orElseThrow(() -> new RuntimeException("Lesson not found"));

        Chapter chapter = lesson.getChapter();
        if (chapter == null || !isChapterAssignedToClass(chapter, classId) || !isChapterVisibleInClass(chapter, classId)) {
            throw new RuntimeException("Lesson not found");
        }
        if (!isLessonVisibleInClass(lesson, classId)) {
            throw new RuntimeException("Lesson not found");
        }

        List<MiniQuizQuestionDTO> miniQuestions = miniQuizQuestionRepository.findByLessonIdOrderByIdAsc(lessonId)
            .stream()
            .map(this::mapMiniQuizQuestion)
            .toList();

        return new LessonDetailDTO(
            lesson.getId(),
            chapter.getId(),
            chapter.getTitle(),
            lesson.getTitle(),
            lesson.getTextContent(),
            lesson.getDurationMinutes(),
            miniQuestions
        );
    }

    private boolean isChapterVisibleInClass(Chapter chapter, UUID classId) {
        if (chapter.getMaterialScope() == MaterialScope.CLASS_PRIVATE) {
            return chapter.getOwnerClass() != null && classId.equals(chapter.getOwnerClass().getId());
        }

        return true;
    }

    private boolean isLessonVisibleInClass(Lesson lesson, UUID classId) {
        if (!Boolean.TRUE.equals(lesson.getPublished())) {
            return false;
        }

        if (lesson.getMaterialScope() == MaterialScope.CLASS_PRIVATE) {
            return lesson.getOwnerClass() != null && classId.equals(lesson.getOwnerClass().getId());
        }

        return true;
    }

    private Chapter requireVisibleClassChapter(String studentUsername, UUID classId, UUID chapterId) {
        StudyClass studyClass = requireEnrolledClass(studentUsername, classId);
        Chapter chapter = studyClass.getChapters().stream()
            .filter(item -> item != null && chapterId.equals(item.getId()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Chapter not found in class"));

        if (!Boolean.TRUE.equals(chapter.getPublished()) || !isChapterVisibleInClass(chapter, classId)) {
            throw new RuntimeException("Chapter not found in class");
        }

        return chapter;
    }

    private boolean isChapterAssignedToClass(Chapter chapter, UUID classId) {
        return chapter.getStudyClasses() != null
            && chapter.getStudyClasses().stream().anyMatch(studyClass -> studyClass != null && classId.equals(studyClass.getId()));
    }

    private MiniQuizQuestionDTO mapMiniQuizQuestion(MiniQuizQuestion question) {
        return new MiniQuizQuestionDTO(
            question.getId(),
            question.getQuestionType() == null ? QuestionType.SINGLE_CHOICE : question.getQuestionType(),
            question.getPrompt(),
            question.getOptionA(),
            question.getOptionB(),
            question.getOptionC(),
            question.getOptionD()
        );
    }

    private StudyClass requireEnrolledClass(String studentUsername, UUID classId) {
        User studentUser = requireStudentUser(studentUsername);
        boolean enrolled = classStudentLinkRepository.findByStudentId(studentUser.getId())
            .stream()
            .anyMatch(link -> link.getClassRoom() != null && link.getClassRoom().getId().equals(classId));
        if (!enrolled) {
            throw new RuntimeException("You are not enrolled in this class");
        }

        return studyClassRepository.findById(classId)
            .orElseThrow(() -> new RuntimeException("Class not found"));
        }

    private ChapterResponse mapChapterToResponse(Chapter chapter) {
        return new ChapterResponse(
            chapter.getId(),
            chapter.getTitle(),
            chapter.getDescription(),
            chapter.getGradeLevel(),
            chapter.getOrderIndex(),
            chapter.getCreatedAt(),
            chapter.getUpdatedAt()
        );
        }
}
