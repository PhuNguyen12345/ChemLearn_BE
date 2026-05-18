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

import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyServiceImpl implements StudyService {
        private final ChapterRepository chapterRepository;
        private final LessonRepository lessonRepository;
        private final MiniQuizQuestionRepository miniQuizQuestionRepository;

        @Override
        public List<StudyChapterDTO> getChaptersWithLessons() {
                return chapterRepository.findByPublishedTrueAndMaterialScopeOrderByOrderIndexAsc(MaterialScope.GLOBAL)
                                .stream()
                                .map(chapter -> new StudyChapterDTO(
                                                chapter.getId(),
                                                chapter.getTitle(),
                                                chapter.getDescription(),
                                                lessonRepository.findByChapterIdAndPublishedTrueAndMaterialScopeOrderByOrderIndexAsc(
                                                                chapter.getId(),
                                                                MaterialScope.GLOBAL)
                                                                .stream()
                                                                .map(lesson -> new LessonSummaryDTO(lesson.getId(), lesson.getTitle(), lesson.getDurationMinutes()))
                                                                .toList()))
                                .toList();
        }

        @Override
        public LessonDetailDTO getLessonDetail(UUID lessonId) {
                Lesson lesson = getVisibleStudyLessonOrThrow(lessonId);
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
                return new LessonDetailDTO(lesson.getId(), lesson.getChapter().getId(), lesson.getChapter().getTitle(), lesson.getTitle(), lesson.getTextContent(), lesson.getDurationMinutes(), miniQuestions);
        }

        @Override
        public MiniQuizSubmitResponseDTO submitMiniQuiz(UUID lessonId, MiniQuizSubmitRequestDTO requestDTO) {
                getVisibleStudyLessonOrThrow(lessonId);
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
}
