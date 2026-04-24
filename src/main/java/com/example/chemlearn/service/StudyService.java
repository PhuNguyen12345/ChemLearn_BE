package com.example.chemlearn.service;

import com.example.chemlearn.dtos.study.LessonDetailDTO;
import com.example.chemlearn.dtos.study.MiniQuizSubmitRequestDTO;
import com.example.chemlearn.dtos.study.MiniQuizSubmitResponseDTO;
import com.example.chemlearn.dtos.study.StudyChapterDTO;

import java.util.List;

public interface StudyService {
    List<StudyChapterDTO> getChaptersWithLessons();

    LessonDetailDTO getLessonDetail(Long lessonId);

    MiniQuizSubmitResponseDTO submitMiniQuiz(Long lessonId, MiniQuizSubmitRequestDTO requestDTO);
}
