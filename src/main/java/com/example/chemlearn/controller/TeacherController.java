package com.example.chemlearn.controller;

import com.example.chemlearn.dtos.teacher.*;
import com.example.chemlearn.entity.*;
import com.example.chemlearn.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
@PreAuthorize("hasRole('TEACHER')")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping("/chapters")
    public List<Chapter> getChapters() {
        return teacherService.getChapters();
    }

    @PostMapping("/chapters")
    public Chapter createChapter(@Valid @RequestBody TeacherChapterRequestDTO dto) {
        return teacherService.createChapter(dto);
    }

    @PutMapping("/chapters/{chapterId}")
    public Chapter updateChapter(@PathVariable Long chapterId, @Valid @RequestBody TeacherChapterRequestDTO dto) {
        return teacherService.updateChapter(chapterId, dto);
    }

    @DeleteMapping("/chapters/{chapterId}")
    public void deleteChapter(@PathVariable Long chapterId) {
        teacherService.deleteChapter(chapterId);
    }

    @GetMapping("/lessons")
    public List<Lesson> getLessons() {
        return teacherService.getLessons();
    }

    @PostMapping("/lessons")
    public Lesson createLesson(@Valid @RequestBody TeacherLessonRequestDTO dto) {
        return teacherService.createLesson(dto);
    }

    @PutMapping("/lessons/{lessonId}")
    public Lesson updateLesson(@PathVariable Long lessonId, @Valid @RequestBody TeacherLessonRequestDTO dto) {
        return teacherService.updateLesson(lessonId, dto);
    }

    @DeleteMapping("/lessons/{lessonId}")
    public void deleteLesson(@PathVariable Long lessonId) {
        teacherService.deleteLesson(lessonId);
    }

    @GetMapping("/quizzes")
    public List<Quiz> getQuizzes(Authentication authentication) {
        return teacherService.getQuizzes(authentication.getName());
    }

    @PostMapping("/quizzes")
    public Quiz createQuiz(@Valid @RequestBody TeacherQuizRequestDTO dto, Authentication authentication) {
        return teacherService.createQuiz(dto, authentication.getName());
    }

    @PutMapping("/quizzes/{quizId}")
    public Quiz updateQuiz(@PathVariable Long quizId, @Valid @RequestBody TeacherQuizRequestDTO dto, Authentication authentication) {
        return teacherService.updateQuiz(quizId, dto, authentication.getName());
    }

    @DeleteMapping("/quizzes/{quizId}")
    public void deleteQuiz(@PathVariable Long quizId, Authentication authentication) {
        teacherService.deleteQuiz(quizId, authentication.getName());
    }

    @GetMapping("/quizzes/{quizId}/questions")
    public List<QuizQuestion> getQuizQuestions(@PathVariable Long quizId, Authentication authentication) {
        return teacherService.getQuizQuestions(quizId, authentication.getName());
    }

    @PostMapping("/quizzes/{quizId}/questions")
    public QuizQuestion createQuizQuestion(@PathVariable Long quizId,
                                           @Valid @RequestBody TeacherQuizQuestionRequestDTO dto,
                                           Authentication authentication) {
        return teacherService.createQuizQuestion(quizId, dto, authentication.getName());
    }

    @PutMapping("/quiz-questions/{questionId}")
    public QuizQuestion updateQuizQuestion(@PathVariable Long questionId,
                                           @Valid @RequestBody TeacherQuizQuestionRequestDTO dto,
                                           Authentication authentication) {
        return teacherService.updateQuizQuestion(questionId, dto, authentication.getName());
    }

    @DeleteMapping("/quiz-questions/{questionId}")
    public void deleteQuizQuestion(@PathVariable Long questionId, Authentication authentication) {
        teacherService.deleteQuizQuestion(questionId, authentication.getName());
    }

    @GetMapping("/assignments")
    public List<Assignment> getAssignments(Authentication authentication) {
        return teacherService.getAssignments(authentication.getName());
    }

    @PostMapping("/assignments")
    public Assignment createAssignment(@Valid @RequestBody TeacherAssignmentRequestDTO dto, Authentication authentication) {
        return teacherService.createAssignment(dto, authentication.getName());
    }

    @PutMapping("/assignments/{assignmentId}")
    public Assignment updateAssignment(@PathVariable Long assignmentId,
                                       @Valid @RequestBody TeacherAssignmentRequestDTO dto,
                                       Authentication authentication) {
        return teacherService.updateAssignment(assignmentId, dto, authentication.getName());
    }

    @DeleteMapping("/assignments/{assignmentId}")
    public void deleteAssignment(@PathVariable Long assignmentId, Authentication authentication) {
        teacherService.deleteAssignment(assignmentId, authentication.getName());
    }

    @GetMapping("/submissions")
    public List<TeacherSubmissionDTO> getSubmissions(Authentication authentication) {
        return teacherService.getSubmissions(authentication.getName());
    }

    @GetMapping("/analytics/students")
    public List<TeacherStudentPerformanceDTO> getStudentPerformance(Authentication authentication) {
        return teacherService.getStudentPerformance(authentication.getName());
    }

    @GetMapping("/summary")
    public TeacherDashboardSummaryDTO getSummary(Authentication authentication) {
        return teacherService.getSummary(authentication.getName());
    }
}
