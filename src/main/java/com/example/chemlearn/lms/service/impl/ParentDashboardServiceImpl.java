package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.lms.dto.parent.ChildGamificationDTO;
import com.example.chemlearn.lms.dto.parent.ChildProfileDTO;
import com.example.chemlearn.lms.dto.parent.ParentDashboardOverviewDTO;
import com.example.chemlearn.lms.dto.parent.ScoreTimelineDTO;
import com.example.chemlearn.lms.entity.QuizAttempt;
import com.example.chemlearn.lms.enums.AttemptStatus;
import com.example.chemlearn.lms.repository.QuizAttemptRepository;
import com.example.chemlearn.lms.repository.StudentRepository;
import com.example.chemlearn.lms.repository.AssignmentRepository;
import com.example.chemlearn.lms.service.ParentDashboardService;
import com.example.chemlearn.gamification.entity.StudentQuest;
import com.example.chemlearn.gamification.repository.StudentQuestRepository;
import com.example.chemlearn.lms.entity.ParentStudentLink;
import com.example.chemlearn.lms.repository.ParentStudentLinkRepository;
import com.example.chemlearn.lms.repository.UserRepository;
import com.example.chemlearn.core.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParentDashboardServiceImpl implements ParentDashboardService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final AssignmentRepository assignmentRepository;
    private final StudentQuestRepository studentQuestRepository;
    private final ParentStudentLinkRepository parentStudentLinkRepository;

    private void validateParentChildRelationship(String username, UUID studentId) {
        UUID parentUserId = getParentIdByUsername(username);

        boolean isLinked = parentStudentLinkRepository.findByParent_IdAndStudent_Id(parentUserId, studentId).isPresent();

        if (!isLinked) {
            throw new IllegalArgumentException("You don't have permission to access this student's data");
        }
    }

    private UUID getParentIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();
    }

    @Override
    public List<ChildProfileDTO> getChildrenByParent(String username) {
        UUID parentId = getParentIdByUsername(username);
        List<ParentStudentLink> links = parentStudentLinkRepository.findByParent_Id(parentId);
        
        return links.stream().map(link -> {
            User studentUser = link.getStudent();
            Student student = studentRepository.findById(studentUser.getId()).orElse(null);
            
            return ChildProfileDTO.builder()
                .studentId(studentUser.getId())
                .fullName(studentUser.getFullName())
                .email(studentUser.getEmail())
                .schoolName(student != null ? student.getSchoolName() : "N/A")
                .gradeLevel(student != null ? student.getGradeLevel() : 0)
                .avatarUrl(studentUser.getAvatarUrl())
                .build();
        }).collect(Collectors.toList());
    }

    @Override
    public ParentDashboardOverviewDTO getChildOverview(String username, UUID studentId) {
        validateParentChildRelationship(username, studentId);

        List<QuizAttempt> recentAttempts = quizAttemptRepository.findTop5ByStudentIdAndStatusOrderBySubmittedAtDesc(studentId, AttemptStatus.COMPLETED);
        
        Double averageScore = 0.0;
        if (!recentAttempts.isEmpty()) {
            double totalScore = recentAttempts.stream()
                    .filter(qa -> qa.getScore() != null)
                    .mapToDouble(qa -> qa.getScore().doubleValue())
                    .sum();
            averageScore = Math.round((totalScore / recentAttempts.size()) * 10.0) / 10.0;
        }

        Long totalAssignments = assignmentRepository.countByStudentId(studentId);
        Long completedAssignments = quizAttemptRepository.countCompletedQuizzesByStudentId(studentId);
        
        Double completionPercentage = 0.0;
        if (totalAssignments != null && totalAssignments > 0) {
            completionPercentage = Math.round(((double) completedAssignments / totalAssignments) * 1000.0) / 10.0;
        }

        return ParentDashboardOverviewDTO.builder()
                .recentAverageScore(averageScore)
                .completedAssignments(completedAssignments != null ? completedAssignments : 0L)
                .totalAssignments(totalAssignments != null ? totalAssignments : 0L)
                .completionPercentage(completionPercentage)
                .build();
    }

    @Override
    public ChildGamificationDTO getChildGamification(String username, UUID studentId) {
        validateParentChildRelationship(username, studentId);

        Student student = studentRepository.findById(studentId).orElseThrow();
        int level = (student.getExperience() / 100) + 1;

        List<StudentQuest> pendingQuests = studentQuestRepository.findByStudentIdAndIsClaimedFalse(studentId);
        
        List<ChildGamificationDTO.PendingQuestDTO> pendingQuestDTOs = pendingQuests.stream()
                .map(sq -> ChildGamificationDTO.PendingQuestDTO.builder()
                        .questTitle(sq.getQuest().getTitle())
                        .description(sq.getQuest().getActionType())
                        .requiredAmount(sq.getQuest().getTargetValue())
                        .currentProgress(sq.getCurrentProgress())
                        .build())
                .collect(Collectors.toList());

        return ChildGamificationDTO.builder()
                .level(level)
                .experience(student.getExperience())
                .totalPoints(student.getTotalPoints())
                .pendingQuests(pendingQuestDTOs)
                .build();
    }

    @Override
    public List<ScoreTimelineDTO> getChildScoreTimeline(String username, UUID studentId) {
        validateParentChildRelationship(username, studentId);

        List<QuizAttempt> attempts = quizAttemptRepository.findTop10ByStudentIdAndStatusOrderBySubmittedAtAsc(studentId, AttemptStatus.COMPLETED);
        
        return attempts.stream()
                .filter(qa -> qa.getScore() != null && qa.getSubmittedAt() != null)
                .map(qa -> ScoreTimelineDTO.builder()
                        .quizTitle(qa.getQuiz() != null ? qa.getQuiz().getTitle() : "Bài kiểm tra")
                        .score(qa.getScore().doubleValue())
                        .submittedAt(qa.getSubmittedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
