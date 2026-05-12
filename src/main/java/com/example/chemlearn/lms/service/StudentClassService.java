package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.dto.quiz.QuizListItemDTO;
import com.example.chemlearn.lms.dto.response.StudyClassAssignmentResponse;
import com.example.chemlearn.lms.dto.response.ChapterResponse;
import com.example.chemlearn.lms.dto.response.StudyClassResponse;
import com.example.chemlearn.lms.dto.study.LessonDetailDTO;
import com.example.chemlearn.lms.dto.study.LessonSummaryDTO;
import java.util.UUID;
import java.util.List;

public interface StudentClassService {
    List<StudyClassResponse> getMyClasses(String studentUsername);

    List<QuizListItemDTO> getMyQuizzes(String studentUsername);

    List<QuizListItemDTO> getQuizzesForClass(String studentUsername, UUID classId);

    List<StudyClassAssignmentResponse> getMyAssignments(String studentUsername);

    List<StudyClassAssignmentResponse> getAssignmentsForClass(String studentUsername, UUID classId);

    StudyClassResponse joinClassByCode(String studentUsername, String classCode);

    void leaveClass(String studentUsername, UUID classId);

    List<ChapterResponse> getChaptersForClass(String studentUsername, UUID classId);

    List<LessonSummaryDTO> getLessonsForClassChapter(String studentUsername, UUID classId, UUID chapterId);

    LessonDetailDTO getLessonDetailForClass(String studentUsername, UUID classId, UUID lessonId);
}