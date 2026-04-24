package com.example.chemlearn.service;

import com.example.chemlearn.dtos.teacher.*;
import com.example.chemlearn.entity.*;

import java.util.List;

public interface TeacherService {
    List<Chapter> getChapters();

    Chapter createChapter(TeacherChapterRequestDTO dto);

    Chapter updateChapter(Long chapterId, TeacherChapterRequestDTO dto);

    void deleteChapter(Long chapterId);

    List<Lesson> getLessons();

    Lesson createLesson(TeacherLessonRequestDTO dto);

    Lesson updateLesson(Long lessonId, TeacherLessonRequestDTO dto);

    void deleteLesson(Long lessonId);

    List<Quiz> getQuizzes(String teacherUsername);

    Quiz createQuiz(TeacherQuizRequestDTO dto, String teacherUsername);

    Quiz updateQuiz(Long quizId, TeacherQuizRequestDTO dto, String teacherUsername);

    void deleteQuiz(Long quizId, String teacherUsername);

    List<QuizQuestion> getQuizQuestions(Long quizId, String teacherUsername);

    List<TeacherQuestionBankItemDTO> getQuestionBank(String teacherUsername);

    TeacherQuestionBankItemDTO createQuestionBankItem(TeacherQuestionBankRequestDTO dto, String teacherUsername);

    TeacherQuestionBankItemDTO updateQuestionBankItem(Long bankQuestionId, TeacherQuestionBankRequestDTO dto, String teacherUsername);

    void deleteQuestionBankItem(Long bankQuestionId, String teacherUsername);

    QuizQuestion addQuestionFromBank(Long quizId, Long bankQuestionId, String teacherUsername);

    QuizQuestion createQuizQuestion(Long quizId, TeacherQuizQuestionRequestDTO dto, String teacherUsername);

    QuizQuestion updateQuizQuestion(Long questionId, TeacherQuizQuestionRequestDTO dto, String teacherUsername);

    void deleteQuizQuestion(Long questionId, String teacherUsername);

    List<Assignment> getAssignments(String teacherUsername);

    Assignment createAssignment(TeacherAssignmentRequestDTO dto, String teacherUsername);

    Assignment updateAssignment(Long assignmentId, TeacherAssignmentRequestDTO dto, String teacherUsername);

    void deleteAssignment(Long assignmentId, String teacherUsername);

    List<TeacherSubmissionDTO> getSubmissions(String teacherUsername);

    List<TeacherClassInfoDTO> getAssignedClasses(String teacherUsername);

    TeacherStudentAccountDTO getStudentAccount(Long studentId, String teacherUsername);

    List<TeacherStudentPerformanceDTO> getStudentPerformance(String teacherUsername);

    TeacherDashboardSummaryDTO getSummary(String teacherUsername);
}
