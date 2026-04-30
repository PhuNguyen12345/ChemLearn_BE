package com.example.chemlearn.lms.controller;

import com.example.chemlearn.lms.dto.request.JoinClassRequestDTO;
import com.example.chemlearn.lms.dto.quiz.QuizListItemDTO;
import com.example.chemlearn.lms.dto.response.ChapterResponse;
import com.example.chemlearn.lms.dto.response.StudyClassAssignmentResponse;
import com.example.chemlearn.lms.dto.response.StudyClassResponse;
import com.example.chemlearn.lms.service.StudentClassService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/classes")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class StudentClassController {
    private final StudentClassService studentClassService;

    @GetMapping
    public List<StudyClassResponse> getMyClasses(Authentication authentication) {
        return studentClassService.getMyClasses(authentication.getName());
    }

    @GetMapping("/quizzes")
    public List<QuizListItemDTO> getMyQuizzes(Authentication authentication) {
        return studentClassService.getMyQuizzes(authentication.getName());
    }

    @GetMapping("/assignments")
    public List<StudyClassAssignmentResponse> getMyAssignments(Authentication authentication) {
        return studentClassService.getMyAssignments(authentication.getName());
    }

    @GetMapping("/{classId}/quizzes")
    public List<QuizListItemDTO> getClassQuizzes(@PathVariable java.util.UUID classId, Authentication authentication) {
        return studentClassService.getQuizzesForClass(authentication.getName(), classId);
    }

    @GetMapping("/{classId}/assignments")
    public List<StudyClassAssignmentResponse> getClassAssignments(@PathVariable java.util.UUID classId, Authentication authentication) {
        return studentClassService.getAssignmentsForClass(authentication.getName(), classId);
    }

    @PostMapping("/join")
    public StudyClassResponse joinClass(@Valid @RequestBody JoinClassRequestDTO request, Authentication authentication) {
        return studentClassService.joinClassByCode(authentication.getName(), request.getClassCode());
    }

    @GetMapping("/{classId}/chapters")
    public List<ChapterResponse> getClassChapters(@PathVariable java.util.UUID classId, Authentication authentication) {
        return studentClassService.getChaptersForClass(authentication.getName(), classId);
    }
}