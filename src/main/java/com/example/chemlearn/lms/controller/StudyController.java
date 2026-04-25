package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.study.LessonDetailDTO;
import com.example.chemlearn.lms.dto.study.MiniQuizSubmitRequestDTO;
import com.example.chemlearn.lms.dto.study.MiniQuizSubmitResponseDTO;
import com.example.chemlearn.lms.dto.study.StudyChapterDTO;
import com.example.chemlearn.lms.service.StudyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/study")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    @GetMapping("/chapters")
    public List<StudyChapterDTO> getChapters() {
        return studyService.getChaptersWithLessons();
    }

    @GetMapping("/lessons/{lessonId}")
    public LessonDetailDTO getLesson(@PathVariable UUID lessonId) {
        return studyService.getLessonDetail(lessonId);
    }

    @PostMapping("/lessons/{lessonId}/mini-quiz/submit")
    public MiniQuizSubmitResponseDTO submitMiniQuiz(
            @PathVariable UUID lessonId,
            @Valid @RequestBody MiniQuizSubmitRequestDTO requestDTO
    ) {
        return studyService.submitMiniQuiz(lessonId, requestDTO);
    }
}

