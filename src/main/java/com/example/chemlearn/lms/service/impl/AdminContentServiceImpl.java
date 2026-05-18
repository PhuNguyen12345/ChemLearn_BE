package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.lms.repository.UserRepository;
import com.example.chemlearn.lms.dto.admin.*;
import com.example.chemlearn.lms.entity.Chapter;
import com.example.chemlearn.lms.entity.Lesson;
import com.example.chemlearn.lms.entity.MiniQuizQuestion;
import com.example.chemlearn.lms.enums.MaterialScope;
import com.example.chemlearn.lms.enums.QuestionType;
import com.example.chemlearn.lms.exception.CustomExceptions;
import com.example.chemlearn.lms.repository.ChapterRepository;
import com.example.chemlearn.lms.repository.LessonRepository;
import com.example.chemlearn.lms.repository.MiniQuizQuestionRepository;
import com.example.chemlearn.lms.service.AdminContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminContentServiceImpl implements AdminContentService {

    private final ChapterRepository chapterRepository;
    private final LessonRepository lessonRepository;
    private final MiniQuizQuestionRepository miniQuizQuestionRepository;
    private final UserRepository userRepository;

    // ==================== HELPER METHODS ====================

    /**
     * Get the admin user by username and verify they have ADMIN role
     */
    private User getAdminUser(String adminUsername) {
        User user = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Admin user not found"));

        if (user.getRole() != UserRole.ROLE_ADMIN) {
            log.warn("Unauthorized admin action attempted by user: {}", adminUsername);
            throw new CustomExceptions.UnauthorizedException("Only admins can perform this action");
        }

        return user;
    }

    private AdminChapterResponseDTO convertChapterToDto(Chapter chapter) {
        int lessonCount = lessonRepository.countByChapterId(chapter.getId());

        return AdminChapterResponseDTO.builder()
                .id(chapter.getId())
                .title(chapter.getTitle())
                .description(chapter.getDescription())
                .orderIndex(chapter.getOrderIndex())
                .published(chapter.getPublished())
                .lessonCount(lessonCount)
                .createdBy(chapter.getCreatedBy() != null ? chapter.getCreatedBy().getId() : null)
                .createdAt(chapter.getCreatedAt())
                .updatedBy(chapter.getUpdatedBy() != null ? chapter.getUpdatedBy().getId() : null)
                .updatedAt(chapter.getUpdatedAt())
                .build();
    }

    private AdminLessonResponseDTO convertLessonToDto(Lesson lesson) {
        List<AdminMiniQuizQuestionDTO> miniQuizQuestions = miniQuizQuestionRepository
                .findByLessonIdOrderByIdAsc(lesson.getId())
                .stream()
                .map(this::convertMiniQuizQuestionToDto)
                .collect(Collectors.toList());

        return AdminLessonResponseDTO.builder()
                .id(lesson.getId())
                .chapterId(lesson.getChapter().getId())
                .chapterTitle(lesson.getChapter().getTitle())
                .title(lesson.getTitle())
                .content(lesson.getTextContent())
                .durationMinutes(lesson.getDurationMinutes())
                .orderIndex(lesson.getOrderIndex())
                .published(lesson.getPublished())
                .miniQuizQuestions(miniQuizQuestions)
                .createdBy(lesson.getCreatedBy() != null ? lesson.getCreatedBy().getId() : null)
                .createdAt(lesson.getCreatedAt())
                .updatedBy(lesson.getUpdatedBy() != null ? lesson.getUpdatedBy().getId() : null)
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }

    private AdminMiniQuizQuestionDTO convertMiniQuizQuestionToDto(MiniQuizQuestion question) {
        return AdminMiniQuizQuestionDTO.builder()
                .id(question.getId())
                .questionType(question.getQuestionType() == null ? QuestionType.SINGLE_CHOICE : question.getQuestionType())
                .questionText(question.getPrompt())
                .optionA(question.getOptionA())
                .optionB(question.getOptionB())
                .optionC(question.getOptionC())
                .optionD(question.getOptionD())
                .correctOption(question.getCorrectOption())
                .orderIndex(question.getId() != null ? 0 : null) // TODO: Add order_index to MiniQuizQuestion
                .build();
    }

    // ==================== CHAPTER OPERATIONS ====================

    @Override
    @Transactional(readOnly = true)
    public List<AdminChapterResponseDTO> getAllChapters() {
        return chapterRepository.findByMaterialScopeOrderByOrderIndexAsc(MaterialScope.GLOBAL).stream()
                .map(this::convertChapterToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminChapterResponseDTO getChapterById(UUID chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Chapter not found"));
        return convertChapterToDto(chapter);
    }

    @Override
    @Transactional
    public AdminChapterResponseDTO createChapter(AdminChapterRequestDTO dto, String adminUsername) {
        User admin = getAdminUser(adminUsername);

        Chapter chapter = new Chapter();
        chapter.setTitle(dto.getTitle());
        chapter.setDescription(dto.getDescription());
        chapter.setOrderIndex(dto.getOrderIndex() != null ? dto.getOrderIndex() : 0);
        chapter.setPublished(dto.getPublished() != null ? dto.getPublished() : true);
        chapter.setCreatedBy(admin);
        chapter.setUpdatedBy(admin);
        chapter.setMaterialScope(MaterialScope.GLOBAL);
        chapter.setOwnerClass(null);
        chapter.setCreatedAt(Instant.now());
        chapter.setUpdatedAt(Instant.now());
        chapter.setGradeLevel(9); // Default grade level

        Chapter savedChapter = chapterRepository.save(chapter);
        log.info("Chapter created: {} by admin: {}", savedChapter.getId(), adminUsername);

        return convertChapterToDto(savedChapter);
    }

    @Override
    @Transactional
    public AdminChapterResponseDTO updateChapter(UUID chapterId, AdminChapterRequestDTO dto, String adminUsername) {
        User admin = getAdminUser(adminUsername);

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Chapter not found"));

        chapter.setTitle(dto.getTitle());
        chapter.setDescription(dto.getDescription());
        if (dto.getOrderIndex() != null) {
            chapter.setOrderIndex(dto.getOrderIndex());
        }
        if (dto.getPublished() != null) {
            chapter.setPublished(dto.getPublished());
        }
        chapter.setMaterialScope(MaterialScope.GLOBAL);
        chapter.setOwnerClass(null);
        chapter.setUpdatedBy(admin);
        chapter.setUpdatedAt(Instant.now());

        Chapter updatedChapter = chapterRepository.save(chapter);
        log.info("Chapter updated: {} by admin: {}", updatedChapter.getId(), adminUsername);

        return convertChapterToDto(updatedChapter);
    }

    @Override
    @Transactional
    public void deleteChapter(UUID chapterId, String adminUsername) {
        User admin = getAdminUser(adminUsername);

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Chapter not found"));

        chapterRepository.delete(chapter);
        log.info("Chapter deleted: {} by admin: {}", chapterId, adminUsername);
    }

    // ==================== LESSON OPERATIONS ====================

    @Override
    @Transactional(readOnly = true)
    public List<AdminLessonResponseDTO> getLessonsByChapter(UUID chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Chapter not found"));

        return lessonRepository.findByChapterIdOrderByOrderIndexAsc(chapterId).stream()
                .map(this::convertLessonToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminLessonResponseDTO getLessonById(UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Lesson not found"));
        return convertLessonToDto(lesson);
    }

    @Override
    @Transactional
    public AdminLessonResponseDTO createLesson(AdminLessonRequestDTO dto, String adminUsername) {
        User admin = getAdminUser(adminUsername);

        Chapter chapter = chapterRepository.findById(dto.getChapterId())
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Chapter not found"));

        Lesson lesson = new Lesson();
        lesson.setChapter(chapter);
        lesson.setTitle(dto.getTitle());
        lesson.setTextContent(dto.getContent());
        lesson.setContentType("TEXT"); // Default content type
        lesson.setDurationMinutes(dto.getDurationMinutes() != null ? dto.getDurationMinutes() : 15);
        lesson.setOrderIndex(dto.getOrderIndex() != null ? dto.getOrderIndex() : 0);
        lesson.setPublished(dto.getPublished() != null ? dto.getPublished() : true);
        lesson.setCreatedBy(admin);
        lesson.setUpdatedBy(admin);
        lesson.setMaterialScope(MaterialScope.GLOBAL);
        lesson.setOwnerClass(null);
        lesson.setCreatedAt(Instant.now());
        lesson.setUpdatedAt(Instant.now());

        Lesson savedLesson = lessonRepository.save(lesson);

        // Add mini-quiz questions if provided
        if (dto.getMiniQuizQuestions() != null && !dto.getMiniQuizQuestions().isEmpty()) {
            for (AdminMiniQuizQuestionDTO questionDto : dto.getMiniQuizQuestions()) {
                addMiniQuizQuestion(savedLesson.getId(), questionDto, adminUsername);
            }
        }

        log.info("Lesson created: {} in chapter: {} by admin: {}", 
                 savedLesson.getId(), chapter.getId(), adminUsername);

        return convertLessonToDto(savedLesson);
    }

    @Override
    @Transactional
    public AdminLessonResponseDTO updateLesson(UUID lessonId, AdminLessonRequestDTO dto, String adminUsername) {
        User admin = getAdminUser(adminUsername);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Lesson not found"));

        lesson.setTitle(dto.getTitle());
        lesson.setTextContent(dto.getContent());
        if (dto.getDurationMinutes() != null) {
            lesson.setDurationMinutes(dto.getDurationMinutes());
        }
        if (dto.getOrderIndex() != null) {
            lesson.setOrderIndex(dto.getOrderIndex());
        }
        if (dto.getPublished() != null) {
            lesson.setPublished(dto.getPublished());
        }
        lesson.setMaterialScope(MaterialScope.GLOBAL);
        lesson.setOwnerClass(null);
        lesson.setUpdatedBy(admin);
        lesson.setUpdatedAt(Instant.now());

        Lesson updatedLesson = lessonRepository.save(lesson);
        log.info("Lesson updated: {} by admin: {}", updatedLesson.getId(), adminUsername);

        return convertLessonToDto(updatedLesson);
    }

    @Override
    @Transactional
    public void deleteLesson(UUID lessonId, String adminUsername) {
        User admin = getAdminUser(adminUsername);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Lesson not found"));

        lessonRepository.delete(lesson);
        log.info("Lesson deleted: {} by admin: {}", lessonId, adminUsername);
    }

    // ==================== MINI-QUIZ OPERATIONS ====================

    @Override
    @Transactional(readOnly = true)
    public List<AdminMiniQuizQuestionDTO> getMiniQuizQuestionsByLesson(UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Lesson not found"));

        return miniQuizQuestionRepository.findByLessonIdOrderByIdAsc(lessonId).stream()
                .map(this::convertMiniQuizQuestionToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AdminMiniQuizQuestionDTO addMiniQuizQuestion(UUID lessonId, AdminMiniQuizQuestionDTO dto, String adminUsername) {
        User admin = getAdminUser(adminUsername);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Lesson not found"));

        // Validate correct option
        validateCorrectOption(dto.getCorrectOption());

        MiniQuizQuestion question = new MiniQuizQuestion();
        question.setLesson(lesson);
        QuestionType questionType = dto.getQuestionType() == null ? QuestionType.SINGLE_CHOICE : dto.getQuestionType();

        question.setPrompt(dto.getQuestionText());
        question.setQuestionType(questionType);
        question.setOptionA(dto.getOptionA());
        question.setOptionB(dto.getOptionB());
        question.setOptionC(dto.getOptionC());
        question.setOptionD(dto.getOptionD());
        question.setCorrectOption(normalizeCorrectOption(dto.getCorrectOption()));
        question.setExplanation(""); // Can be added later
        question.setCreatedBy(admin);

        MiniQuizQuestion savedQuestion = miniQuizQuestionRepository.save(question);
        log.info("Mini-quiz question added: {} to lesson: {} by admin: {}", 
                 savedQuestion.getId(), lessonId, adminUsername);

        return convertMiniQuizQuestionToDto(savedQuestion);
    }

    @Override
    @Transactional
    public AdminMiniQuizQuestionDTO updateMiniQuizQuestion(UUID questionId, AdminMiniQuizQuestionDTO dto, String adminUsername) {
        User admin = getAdminUser(adminUsername);

        MiniQuizQuestion question = miniQuizQuestionRepository.findById(questionId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Mini-quiz question not found"));

        // Validate correct option
        validateCorrectOption(dto.getCorrectOption());

        QuestionType questionType = dto.getQuestionType() == null ? QuestionType.SINGLE_CHOICE : dto.getQuestionType();

        question.setPrompt(dto.getQuestionText());
        question.setQuestionType(questionType);
        question.setOptionA(dto.getOptionA());
        question.setOptionB(dto.getOptionB());
        question.setOptionC(dto.getOptionC());
        question.setOptionD(dto.getOptionD());
        question.setCorrectOption(normalizeCorrectOption(dto.getCorrectOption()));

        MiniQuizQuestion updatedQuestion = miniQuizQuestionRepository.save(question);
        log.info("Mini-quiz question updated: {} by admin: {}", updatedQuestion.getId(), adminUsername);

        return convertMiniQuizQuestionToDto(updatedQuestion);
    }

    @Override
    @Transactional
    public void deleteMiniQuizQuestion(UUID questionId, String adminUsername) {
        User admin = getAdminUser(adminUsername);

        MiniQuizQuestion question = miniQuizQuestionRepository.findById(questionId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Mini-quiz question not found"));

        miniQuizQuestionRepository.delete(question);
        log.info("Mini-quiz question deleted: {} by admin: {}", questionId, adminUsername);
    }

    // ==================== VALIDATION ====================

    private void validateCorrectOption(String correctOption) {
        String option = normalizeCorrectOption(correctOption);
        if (!option.matches("^[A-D](,[A-D])*$")) {
            throw new CustomExceptions.BadRequestException("Correct option must contain only A, B, C, or D");
        }
    }

    private String normalizeCorrectOption(String correctOption) {
        return java.util.Arrays.stream(correctOption.toUpperCase().split(","))
                .map(String::trim)
                .filter(option -> !option.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.joining(","));
    }
}
