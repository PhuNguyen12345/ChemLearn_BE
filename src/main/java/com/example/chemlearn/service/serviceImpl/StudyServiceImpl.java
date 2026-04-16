package com.example.chemlearn.service.serviceImpl;

import com.example.chemlearn.dtos.study.*;
import com.example.chemlearn.entity.Chapter;
import com.example.chemlearn.entity.Lesson;
import com.example.chemlearn.entity.MiniQuizQuestion;
import com.example.chemlearn.exception.CustomExceptions;
import com.example.chemlearn.repository.ChapterRepository;
import com.example.chemlearn.repository.LessonRepository;
import com.example.chemlearn.repository.MiniQuizQuestionRepository;
import com.example.chemlearn.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyServiceImpl implements StudyService {

    private final ChapterRepository chapterRepository;
    private final LessonRepository lessonRepository;
    private final MiniQuizQuestionRepository miniQuizQuestionRepository;

    @Override
    public List<StudyChapterDTO> getChaptersWithLessons() {
        List<Chapter> chapters = chapterRepository.findByPublishedTrueOrderByDisplayOrderAsc();
        return chapters.stream()
                .map(chapter -> {
                    List<LessonSummaryDTO> lessons = lessonRepository
                            .findByChapterIdAndPublishedTrueOrderByDisplayOrderAsc(chapter.getId())
                            .stream()
                            .map(lesson -> new LessonSummaryDTO(
                                    lesson.getId(),
                                    lesson.getTitle(),
                                    lesson.getEstimatedMinutes()
                            ))
                            .toList();

                    return new StudyChapterDTO(
                            chapter.getId(),
                            chapter.getTitle(),
                            chapter.getDescription(),
                            lessons
                    );
                })
                .toList();
    }

    @Override
    public LessonDetailDTO getLessonDetail(Long lessonId) {
        Lesson lesson = lessonRepository.findByIdAndPublishedTrue(lessonId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Lesson not found"));

        List<MiniQuizQuestionDTO> miniQuestions = miniQuizQuestionRepository
                .findByLessonIdOrderByIdAsc(lessonId)
                .stream()
                .map(question -> new MiniQuizQuestionDTO(
                        question.getId(),
                        question.getPrompt(),
                        question.getOptionA(),
                        question.getOptionB(),
                        question.getOptionC(),
                        question.getOptionD()
                ))
                .toList();

        return new LessonDetailDTO(
                lesson.getId(),
                lesson.getChapter().getId(),
                lesson.getChapter().getTitle(),
                lesson.getTitle(),
                lesson.getContent(),
                lesson.getEstimatedMinutes(),
                miniQuestions
        );
    }

    @Override
    public MiniQuizSubmitResponseDTO submitMiniQuiz(Long lessonId, MiniQuizSubmitRequestDTO requestDTO) {
        List<MiniQuizQuestion> questions = miniQuizQuestionRepository.findByLessonIdOrderByIdAsc(lessonId);
        if (questions.isEmpty()) {
            throw new CustomExceptions.BadRequestException("This lesson does not have a mini quiz");
        }

        Map<Long, String> answerMap = requestDTO.getAnswers()
                .stream()
                .collect(Collectors.toMap(MiniQuizAnswerDTO::getQuestionId, dto -> dto.getSelectedOption().toUpperCase(), (a, b) -> b));

        // Validation for illegal combinations: answer question IDs must belong to this lesson only.
        Set<Long> validQuestionIds = questions.stream().map(MiniQuizQuestion::getId).collect(Collectors.toSet());
        for (Long submittedQuestionId : answerMap.keySet()) {
            if (!validQuestionIds.contains(submittedQuestionId)) {
                throw new CustomExceptions.BadRequestException("Submitted question does not belong to this lesson");
            }
        }

        int total = questions.size();
        int correct = 0;
        for (MiniQuizQuestion question : questions) {
            String selected = answerMap.get(question.getId());
            if (selected != null && selected.equalsIgnoreCase(question.getCorrectOption())) {
                correct++;
            }
        }

        int score = Math.round((correct * 100.0f) / total);
        boolean passed = score >= 70;
        return new MiniQuizSubmitResponseDTO(total, correct, score, passed);
    }
}
