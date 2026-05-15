package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.teacher.*;
import com.example.chemlearn.lms.entity.*;
import com.example.chemlearn.lms.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teacher")
@PreAuthorize("hasRole('TEACHER')")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping("/classes")
    public List<TeacherClassInfoDTO> getAssignedClasses(Authentication authentication) {
        return teacherService.getAssignedClasses(authentication.getName());
    }

    @PostMapping("/classes")
    public TeacherClassInfoDTO createClass(@Valid @RequestBody TeacherClassRequestDTO dto, Authentication authentication) {
        return teacherService.createClass(dto, authentication.getName());
    }

    @PutMapping("/classes/{classId}")
    public TeacherClassInfoDTO updateClass(@PathVariable UUID classId,
                                           @Valid @RequestBody TeacherClassRequestDTO dto,
                                           Authentication authentication) {
        return teacherService.updateClass(classId, dto, authentication.getName());
    }

    @DeleteMapping("/classes/{classId}")
    public void deleteClass(@PathVariable UUID classId, Authentication authentication) {
        teacherService.deleteClass(classId, authentication.getName());
    }

    @GetMapping("/chapters")
    public List<TeacherChapterResponseDTO> getChapters(Authentication authentication) {
        return teacherService.getChapters(authentication.getName());
    }

    @PostMapping("/classes/{classId}/chapters/{chapterId}")
    public void addChapterToClass(@PathVariable java.util.UUID classId,
                                  @PathVariable java.util.UUID chapterId,
                                  Authentication authentication) {
        teacherService.addChapterToClass(classId, chapterId, authentication.getName());
    }

    @DeleteMapping("/classes/{classId}/chapters/{chapterId}")
    public void removeChapterFromClass(@PathVariable java.util.UUID classId,
                                       @PathVariable java.util.UUID chapterId,
                                       Authentication authentication) {
        teacherService.removeChapterFromClass(classId, chapterId, authentication.getName());
    }

    @DeleteMapping("/classes/{classId}/students/{studentId}")
    public void removeStudentFromClass(@PathVariable java.util.UUID classId,
                                       @PathVariable java.util.UUID studentId,
                                       Authentication authentication) {
        teacherService.removeStudentFromClass(classId, studentId, authentication.getName());
    }

    @PostMapping("/chapters")
    public TeacherChapterResponseDTO createChapter(@Valid @RequestBody TeacherChapterRequestDTO dto, Authentication authentication) {
        return teacherService.createChapter(dto, authentication.getName());
    }

    @PutMapping("/chapters/{chapterId}")
    public TeacherChapterResponseDTO updateChapter(@PathVariable UUID chapterId, @Valid @RequestBody TeacherChapterRequestDTO dto, Authentication authentication) {
        return teacherService.updateChapter(chapterId, dto, authentication.getName());
    }

    @DeleteMapping("/chapters/{chapterId}")
    public void deleteChapter(@PathVariable UUID chapterId, Authentication authentication) {
        teacherService.deleteChapter(chapterId, authentication.getName());
    }

    @GetMapping("/lessons")
    public List<TeacherLessonResponseDTO> getLessons(Authentication authentication) {
        return teacherService.getLessons(authentication.getName());
    }

    @PostMapping("/lessons")
    public TeacherLessonResponseDTO createLesson(@Valid @RequestBody TeacherLessonRequestDTO dto, Authentication authentication) {
        return teacherService.createLesson(dto, authentication.getName());
    }

    @PutMapping("/lessons/{lessonId}")
    public TeacherLessonResponseDTO updateLesson(@PathVariable UUID lessonId, @Valid @RequestBody TeacherLessonRequestDTO dto, Authentication authentication) {
        return teacherService.updateLesson(lessonId, dto, authentication.getName());
    }

    @DeleteMapping("/lessons/{lessonId}")
    public void deleteLesson(@PathVariable UUID lessonId, Authentication authentication) {
        teacherService.deleteLesson(lessonId, authentication.getName());
    }

    @GetMapping("/quizzes")
    public List<TeacherQuizResponseDTO> getQuizzes(Authentication authentication) {
        return teacherService.getQuizzes(authentication.getName());
    }

    @PostMapping("/quizzes")
    public TeacherQuizResponseDTO createQuiz(@Valid @RequestBody TeacherQuizRequestDTO dto, Authentication authentication) {
        return teacherService.createQuiz(dto, authentication.getName());
    }

    @PutMapping("/quizzes/{quizId}")
    public TeacherQuizResponseDTO updateQuiz(@PathVariable UUID quizId, @Valid @RequestBody TeacherQuizRequestDTO dto, Authentication authentication) {
        return teacherService.updateQuiz(quizId, dto, authentication.getName());
    }

    @DeleteMapping("/quizzes/{quizId}")
    public void deleteQuiz(@PathVariable UUID quizId, Authentication authentication) {
        teacherService.deleteQuiz(quizId, authentication.getName());
    }

    @GetMapping("/quizzes/{quizId}/questions")
    public List<QuizQuestion> getQuizQuestions(@PathVariable UUID quizId, Authentication authentication) {
        return teacherService.getQuizQuestions(quizId, authentication.getName());
    }

    @GetMapping("/question-bank")
    public List<TeacherQuestionBankItemDTO> getQuestionBank(Authentication authentication) {
        return teacherService.getQuestionBank(authentication.getName());
    }

    @PostMapping("/question-bank")
    public TeacherQuestionBankItemDTO createQuestionBankItem(@Valid @RequestBody TeacherQuestionBankRequestDTO dto,
                                                             Authentication authentication) {
        return teacherService.createQuestionBankItem(dto, authentication.getName());
    }

    @PutMapping("/question-bank/{bankQuestionId}")
    public TeacherQuestionBankItemDTO updateQuestionBankItem(@PathVariable UUID bankQuestionId,
                                                             @Valid @RequestBody TeacherQuestionBankRequestDTO dto,
                                                             Authentication authentication) {
        return teacherService.updateQuestionBankItem(bankQuestionId, dto, authentication.getName());
    }

    @DeleteMapping("/question-bank/{bankQuestionId}")
    public void deleteQuestionBankItem(@PathVariable UUID bankQuestionId, Authentication authentication) {
        teacherService.deleteQuestionBankItem(bankQuestionId, authentication.getName());
    }

    @PostMapping("/quizzes/{quizId}/questions/from-bank/{bankQuestionId}")
    public QuizQuestion addQuestionFromBank(@PathVariable UUID quizId,
                                            @PathVariable UUID bankQuestionId,
                                            Authentication authentication) {
        return teacherService.addQuestionFromBank(quizId, bankQuestionId, authentication.getName());
    }

    @PostMapping("/quizzes/{quizId}/questions")
    public QuizQuestion createQuizQuestion(@PathVariable UUID quizId,
                                           @Valid @RequestBody TeacherQuizQuestionRequestDTO dto,
                                           Authentication authentication) {
        return teacherService.createQuizQuestion(quizId, dto, authentication.getName());
    }

    @PutMapping("/quiz-questions/{questionId}")
    public QuizQuestion updateQuizQuestion(@PathVariable UUID questionId,
                                           @Valid @RequestBody TeacherQuizQuestionRequestDTO dto,
                                           Authentication authentication) {
        return teacherService.updateQuizQuestion(questionId, dto, authentication.getName());
    }

    @DeleteMapping("/quiz-questions/{questionId}")
    public void deleteQuizQuestion(@PathVariable UUID questionId, Authentication authentication) {
        teacherService.deleteQuizQuestion(questionId, authentication.getName());
    }

    @GetMapping("/assignments")
    public List<TeacherAssignmentDTO> getAssignments(Authentication authentication) {
        return teacherService.getAssignments(authentication.getName());
    }

    @PostMapping("/assignments")
    public TeacherAssignmentDTO createAssignment(@Valid @RequestBody TeacherAssignmentRequestDTO dto, Authentication authentication) {
        return teacherService.createAssignment(dto, authentication.getName());
    }

    @PutMapping("/assignments/{assignmentId}")
    public TeacherAssignmentDTO updateAssignment(@PathVariable UUID assignmentId,
                                       @Valid @RequestBody TeacherAssignmentRequestDTO dto,
                                       Authentication authentication) {
        return teacherService.updateAssignment(assignmentId, dto, authentication.getName());
    }

    @DeleteMapping("/assignments/{assignmentId}")
    public void deleteAssignment(@PathVariable UUID assignmentId, Authentication authentication) {
        teacherService.deleteAssignment(assignmentId, authentication.getName());
    }

    @GetMapping("/submissions")
    public List<TeacherSubmissionDTO> getSubmissions(Authentication authentication) {
        return teacherService.getSubmissions(authentication.getName());
    }

    @GetMapping("/students/{studentId}")
    public TeacherStudentAccountDTO getStudentAccount(@PathVariable UUID studentId, Authentication authentication) {
        return teacherService.getStudentAccount(studentId, authentication.getName());
    }

    @GetMapping("/analytics/students")
    public List<TeacherStudentPerformanceDTO> getStudentPerformance(Authentication authentication) {
        return teacherService.getStudentPerformance(authentication.getName());
    }

    @GetMapping("/submissions/{attemptId}")
    public TeacherSubmissionDetailDTO getSubmissionDetail(@PathVariable UUID attemptId, Authentication authentication) {
        return teacherService.getSubmissionDetail(attemptId, authentication.getName());
    }

    @PostMapping("/submissions/{attemptId}/grade")
    public void gradeSubmission(@PathVariable UUID attemptId, @RequestBody TeacherGradeRequestDTO dto, Authentication authentication) {
        teacherService.gradeSubmission(attemptId, dto, authentication.getName());
    }

    @GetMapping("/summary")
    public TeacherDashboardSummaryDTO getSummary(Authentication authentication) {
        return teacherService.getSummary(authentication.getName());
    }
}

