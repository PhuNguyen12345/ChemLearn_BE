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

    List<Chapter> getChapters();

    Chapter createChapter(TeacherChapterRequestDTO dto);

    Chapter updateChapter(UUID chapterId, TeacherChapterRequestDTO dto);

    void deleteChapter(UUID chapterId);

    List<Lesson> getLessons();

    Lesson createLesson(TeacherLessonRequestDTO dto);

    Lesson updateLesson(UUID lessonId, TeacherLessonRequestDTO dto);

    void deleteLesson(UUID lessonId);

    List<Quiz> getQuizzes(String teacherUsername);

    Quiz createQuiz(TeacherQuizRequestDTO dto, String teacherUsername);

    Quiz updateQuiz(UUID quizId, TeacherQuizRequestDTO dto, String teacherUsername);

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

    List<Assignment> getAssignments(String teacherUsername);

    Assignment createAssignment(TeacherAssignmentRequestDTO dto, String teacherUsername);

    Assignment updateAssignment(UUID assignmentId, TeacherAssignmentRequestDTO dto, String teacherUsername);

    void deleteAssignment(UUID assignmentId, String teacherUsername);

    List<TeacherSubmissionDTO> getSubmissions(String teacherUsername);

    TeacherStudentAccountDTO getStudentAccount(UUID studentId, String teacherUsername);

    List<TeacherStudentPerformanceDTO> getStudentPerformance(String teacherUsername);

    TeacherDashboardSummaryDTO getSummary(String teacherUsername);
    void addChapterToClass(java.util.UUID classId, java.util.UUID chapterId, String teacherUsername);
    void removeChapterFromClass(java.util.UUID classId, java.util.UUID chapterId, String teacherUsername);
}

