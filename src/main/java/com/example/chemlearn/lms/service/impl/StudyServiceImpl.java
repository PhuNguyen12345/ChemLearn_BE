package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.lms.dto.study.LessonDetailDTO;
import com.example.chemlearn.lms.dto.study.LessonSummaryDTO;
import com.example.chemlearn.lms.dto.study.MiniQuizAnswerDTO;
import com.example.chemlearn.lms.dto.study.MiniQuizQuestionDTO;
import com.example.chemlearn.lms.dto.study.MiniQuizSubmitRequestDTO;
import com.example.chemlearn.lms.dto.study.MiniQuizSubmitResponseDTO;
import com.example.chemlearn.lms.dto.study.StudyChapterDTO;
import com.example.chemlearn.lms.entity.Chapter;
import com.example.chemlearn.lms.entity.Lesson;
import com.example.chemlearn.lms.entity.MiniQuizQuestion;
import com.example.chemlearn.lms.enums.MaterialScope;
import com.example.chemlearn.lms.enums.QuestionType;
import com.example.chemlearn.lms.exception.CustomExceptions;
import com.example.chemlearn.lms.repository.ChapterRepository;
import com.example.chemlearn.lms.repository.LessonRepository;
import com.example.chemlearn.lms.repository.MiniQuizQuestionRepository;
import com.example.chemlearn.lms.service.StudyService;
import com.example.chemlearn.payment.entity.UserPackageEntitlement;
import com.example.chemlearn.payment.enums.EntitlementStatus;
import com.example.chemlearn.payment.repository.UserPackageEntitlementRepository;
import com.example.chemlearn.util.SecurityUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class StudyServiceImpl implements StudyService {
        private final ChapterRepository chapterRepository;
        private final LessonRepository lessonRepository;
        private final MiniQuizQuestionRepository miniQuizQuestionRepository;
        private final UserPackageEntitlementRepository userPackageEntitlementRepository;

        @Override
        public List<StudyChapterDTO> getChaptersWithLessons() {
                UUID currentUserId = SecurityUtils.getCurrentUserId();
                return chapterRepository.findByPublishedTrueAndMaterialScopeOrderByOrderIndexAsc(MaterialScope.GLOBAL)
                                .stream()
                                .map(chapter -> toStudyChapterDTO(chapter, currentUserId))
                                .toList();
        }

        @Override
        public LessonDetailDTO getLessonDetail(UUID lessonId) {
                Lesson lesson = getVisibleStudyLessonOrThrow(lessonId);
                ensureChapterAccess(lesson.getChapter());
                List<MiniQuizQuestionDTO> miniQuestions = miniQuizQuestionRepository.findByLessonIdOrderByIdAsc(lessonId).stream()
                                .map(question -> new MiniQuizQuestionDTO(
                                                question.getId(),
                                                question.getQuestionType() == null ? QuestionType.SINGLE_CHOICE : question.getQuestionType(),
                                                question.getPrompt(),
                                                question.getOptionA(),
                                                question.getOptionB(),
                                                question.getOptionC(),
                                                question.getOptionD()))
                                .toList();
                return new LessonDetailDTO(lesson.getId(), lesson.getChapter().getId(), lesson.getChapter().getTitle(), lesson.getTitle(), lesson.getTextContent(), lesson.getDurationMinutes(), lesson.getVideoUrl(), miniQuestions);
        }

        @Override
        public MiniQuizSubmitResponseDTO submitMiniQuiz(UUID lessonId, MiniQuizSubmitRequestDTO requestDTO) {
                Lesson lesson = getVisibleStudyLessonOrThrow(lessonId);
                ensureChapterAccess(lesson.getChapter());
                List<MiniQuizQuestion> questions = miniQuizQuestionRepository.findByLessonIdOrderByIdAsc(lessonId);
                if (questions.isEmpty()) {
                        throw new CustomExceptions.BadRequestException("This lesson does not have a mini quiz");
                }
                Map<UUID, String> answerMap = requestDTO.getAnswers().stream()
                                .collect(Collectors.toMap(MiniQuizAnswerDTO::getQuestionId, dto -> normalizeOptions(dto.getSelectedOption()), (left, right) -> right));
                Set<UUID> validQuestionIds = questions.stream().map(MiniQuizQuestion::getId).collect(Collectors.toSet());
                for (UUID submittedQuestionId : answerMap.keySet()) {
                        if (!validQuestionIds.contains(submittedQuestionId)) {
                                throw new CustomExceptions.BadRequestException("Submitted question does not belong to this lesson");
                        }
                }
                int total = questions.size();
                int correct = 0;
                for (MiniQuizQuestion question : questions) {
                        String selected = answerMap.get(question.getId());
                        if (selected != null && selected.equals(normalizeOptions(question.getCorrectOption()))) {
                                correct++;
                        }
                }
                int score = Math.round((correct * 100.0f) / total);
                boolean passed = score >= 70;
                return new MiniQuizSubmitResponseDTO(total, correct, score, passed);
        }

        private String normalizeOptions(String options) {
                if (options == null) {
                        return "";
                }
                return Arrays.stream(options.toUpperCase().split(","))
                                .map(String::trim)
                                .filter(option -> !option.isBlank())
                                .distinct()
                                .sorted()
                                .collect(Collectors.joining(","));
        }

        private Lesson getVisibleStudyLessonOrThrow(UUID lessonId) {
                Lesson lesson = lessonRepository.findByIdAndPublishedTrueAndMaterialScope(
                                                lessonId,
                                                MaterialScope.GLOBAL)
                                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Lesson not found"));

                if (!Boolean.TRUE.equals(lesson.getChapter().getPublished())
                                || lesson.getChapter().getMaterialScope() != MaterialScope.GLOBAL) {
                        throw new CustomExceptions.ResourceNotFoundException("Lesson not found");
                }

                return lesson;
        }

        private StudyChapterDTO toStudyChapterDTO(Chapter chapter, UUID currentUserId) {
                String requiredPackageCode = resolveRequiredPackageCode(chapter);
                boolean needPurchase = Boolean.TRUE.equals(chapter.getNeedPurchase());
                boolean hasAccess = !needPurchase || hasActiveEntitlement(currentUserId, requiredPackageCode);

                return new StudyChapterDTO(
                                chapter.getId(),
                                chapter.getTitle(),
                                chapter.getDescription(),
                                chapter.getGradeLevel(),
                                needPurchase,
                                hasAccess,
                                needPurchase ? requiredPackageCode : null,
                                lessonRepository.findByChapterIdAndPublishedTrueAndMaterialScopeOrderByOrderIndexAsc(
                                                chapter.getId(),
                                                MaterialScope.GLOBAL)
                                                .stream()
                                                .map(lesson -> new LessonSummaryDTO(lesson.getId(), lesson.getTitle(), lesson.getDurationMinutes()))
                                                .toList());
        }

        private void ensureChapterAccess(Chapter chapter) {
                if (!Boolean.TRUE.equals(chapter.getNeedPurchase())) {
                        return;
                }

                if (!hasActiveEntitlement(SecurityUtils.getCurrentUserId(), resolveRequiredPackageCode(chapter))) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Study zone package is required for this grade");
                }
        }

        private boolean hasActiveEntitlement(UUID userId, String packageCode) {
                if (userId == null || packageCode == null) {
                        return false;
                }

                return userPackageEntitlementRepository
                                .findByUserIdAndPackageCodeAndStatus(userId, packageCode, EntitlementStatus.ACTIVE)
                                .map(this::entitlementIsCurrent)
                                .orElse(false);
        }

        private boolean entitlementIsCurrent(UserPackageEntitlement entitlement) {
                return entitlement.getEndAt() == null || entitlement.getEndAt().isAfter(LocalDateTime.now());
        }

        private String resolveRequiredPackageCode(Chapter chapter) {
                return chapter.getGradeLevel() == null ? null : "GRADE_" + chapter.getGradeLevel();
        }
}
