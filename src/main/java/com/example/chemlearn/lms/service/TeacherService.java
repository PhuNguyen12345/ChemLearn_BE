package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.dto.teacher.*;
import com.example.chemlearn.lms.entity.*;

import java.util.List;
import java.util.UUID;

public interface TeacherService {
    List<TeacherClassInfoDTO> getAssignedClasses(String teacherUsername);

    TeacherClassInfoDTO createClass(TeacherClassRequestDTO dto, String teacherUsername);

    TeacherClassInfoDTO updateClass(UUID classId, TeacherClassRequestDTO dto, String teacherUsername);

    void deleteClass(UUID classId, String teacherUsername);

    List<TeacherChapterResponseDTO> getChapters(String teacherUsername);
    TeacherChapterResponseDTO createChapter(TeacherChapterRequestDTO dto, String teacherUsername);
    TeacherChapterResponseDTO updateChapter(UUID chapterId, TeacherChapterRequestDTO dto, String teacherUsername);
    void deleteChapter(UUID chapterId, String teacherUsername);

    List<TeacherLessonResponseDTO> getLessons(String teacherUsername);
    TeacherLessonResponseDTO createLesson(TeacherLessonRequestDTO dto, String teacherUsername);
    TeacherLessonResponseDTO updateLesson(UUID lessonId, TeacherLessonRequestDTO dto, String teacherUsername);
    void deleteLesson(UUID lessonId, String teacherUsername);

    List<TeacherQuizResponseDTO> getQuizzes(String teacherUsername);

    TeacherQuizResponseDTO createQuiz(TeacherQuizRequestDTO dto, String teacherUsername);

    TeacherQuizResponseDTO updateQuiz(UUID quizId, TeacherQuizRequestDTO dto, String teacherUsername);

    void deleteQuiz(UUID quizId, String teacherUsername);

    List<QuizQuestion> getQuizQuestions(UUID quizId, String teacherUsername);

    List<TeacherQuestionBankItemDTO> getQuestionBank(String teacherUsername);

    TeacherQuestionBankItemDTO createQuestionBankItem(TeacherQuestionBankRequestDTO dto, String teacherUsername);

    TeacherQuestionBankItemDTO updateQuestionBankItem(UUID bankQuestionId, TeacherQuestionBankRequestDTO dto, String teacherUsername);

    void deleteQuestionBankItem(UUID bankQuestionId, String teacherUsername);

    QuizQuestion addQuestionFromBank(UUID quizId, UUID bankQuestionId, String teacherUsername);

    QuizQuestion createQuizQuestion(UUID quizId, TeacherQuizQuestionRequestDTO dto, String teacherUsername);

    QuizQuestion updateQuizQuestion(UUID questionId, TeacherQuizQuestionRequestDTO dto, String teacherUsername);

    void deleteQuizQuestion(UUID questionId, String teacherUsername);

    List<TeacherAssignmentDTO> getAssignments(String teacherUsername);

    TeacherAssignmentDTO createAssignment(TeacherAssignmentRequestDTO dto, String teacherUsername);

    TeacherAssignmentDTO updateAssignment(UUID assignmentId, TeacherAssignmentRequestDTO dto, String teacherUsername);

    void deleteAssignment(UUID assignmentId, String teacherUsername);

    List<TeacherSubmissionDTO> getSubmissions(String teacherUsername);

    TeacherStudentAccountDTO getStudentAccount(UUID studentId, String teacherUsername);

    List<TeacherStudentPerformanceDTO> getStudentPerformance(String teacherUsername);

    TeacherDashboardSummaryDTO getSummary(String teacherUsername);
    TeacherSubmissionDetailDTO getSubmissionDetail(UUID attemptId, String teacherUsername);
    void gradeSubmission(UUID attemptId, TeacherGradeRequestDTO dto, String teacherUsername);
    void addChapterToClass(java.util.UUID classId, java.util.UUID chapterId, String teacherUsername);
    void removeChapterFromClass(java.util.UUID classId, java.util.UUID chapterId, String teacherUsername);
    void removeStudentFromClass(UUID classId, UUID studentId, String teacherUsername);
}

