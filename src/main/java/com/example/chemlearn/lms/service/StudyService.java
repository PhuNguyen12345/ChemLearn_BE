package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.dto.study.LessonDetailDTO;
import com.example.chemlearn.lms.dto.study.MiniQuizSubmitRequestDTO;
import com.example.chemlearn.lms.dto.study.MiniQuizSubmitResponseDTO;
import com.example.chemlearn.lms.dto.study.StudyChapterDTO;

import java.util.List;
import java.util.UUID;

public interface StudyService {
    List<StudyChapterDTO> getChaptersWithLessons();

    LessonDetailDTO getLessonDetail(UUID lessonId);

    MiniQuizSubmitResponseDTO submitMiniQuiz(UUID lessonId, MiniQuizSubmitRequestDTO requestDTO);
}

