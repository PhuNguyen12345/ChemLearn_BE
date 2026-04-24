package com.example.chemlearn.controller;

import com.example.chemlearn.dtos.study.LessonDetailDTO;
import com.example.chemlearn.dtos.study.MiniQuizSubmitRequestDTO;
import com.example.chemlearn.dtos.study.MiniQuizSubmitResponseDTO;
import com.example.chemlearn.dtos.study.StudyChapterDTO;
import com.example.chemlearn.service.StudyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public LessonDetailDTO getLesson(@PathVariable Long lessonId) {
        return studyService.getLessonDetail(lessonId);
    }

    @PostMapping("/lessons/{lessonId}/mini-quiz/submit")
    public MiniQuizSubmitResponseDTO submitMiniQuiz(
            @PathVariable Long lessonId,
            @Valid @RequestBody MiniQuizSubmitRequestDTO requestDTO
    ) {
        return studyService.submitMiniQuiz(lessonId, requestDTO);
    }
}
