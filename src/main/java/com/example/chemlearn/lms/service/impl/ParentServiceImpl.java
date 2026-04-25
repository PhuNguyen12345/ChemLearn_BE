package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.core.entity.Account;
import com.example.chemlearn.lms.dto.parent.ParentAssessmentDTO;
import com.example.chemlearn.lms.dto.parent.ParentChildDTO;
import com.example.chemlearn.lms.dto.parent.ParentChildPerformanceDTO;
import com.example.chemlearn.lms.entity.Assignment;
import com.example.chemlearn.lms.entity.ParentStudentLink;
import com.example.chemlearn.lms.entity.QuizAttempt;
import com.example.chemlearn.lms.enums.AccountRole;
import com.example.chemlearn.lms.enums.AssignmentStatus;
import com.example.chemlearn.lms.exception.CustomExceptions;
import com.example.chemlearn.lms.repository.AccountRepository;
import com.example.chemlearn.lms.repository.AssignmentRepository;
import com.example.chemlearn.lms.repository.ParentStudentLinkRepository;
import com.example.chemlearn.lms.repository.QuizAttemptRepository;
import com.example.chemlearn.lms.service.ParentService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParentServiceImpl implements ParentService {
        private final AccountRepository accountRepository;
        private final ParentStudentLinkRepository parentStudentLinkRepository;
        private final QuizAttemptRepository quizAttemptRepository;
        private final AssignmentRepository assignmentRepository;

        @Override
        public List<ParentChildDTO> getChildren(String parentUsername) {
                Account parent = getParentByUsername(parentUsername);
                return parentStudentLinkRepository.findByParentId(parent.getId()).stream()
                                .map(link -> new ParentChildDTO(link.getStudent().getId(), link.getStudent().getUsername(), link.getStudent().getEmail()))
                                .sorted(Comparator.comparing(ParentChildDTO::getUsername))
                                .toList();
        }

        @Override
        public ParentChildPerformanceDTO getChildPerformance(String parentUsername, UUID childId) {
                Account child = getOwnedChild(parentUsername, childId);
                List<QuizAttempt> attempts = quizAttemptRepository.findByStudentIdOrderByStartedAtDesc(child.getId());
                List<Assignment> assignments = assignmentRepository.findByStudentIdOrderByIdDesc(child.getId());
                int averageScore = attempts.isEmpty() ? 0 : (int) Math.round(attempts.stream().mapToInt(a -> a.getScore() == null ? 0 : a.getScore().intValue()).average().orElse(0));
                long completedAssignments = assignments.stream().filter(a -> a.getStatus() == AssignmentStatus.SUBMITTED || a.getStatus() == AssignmentStatus.REVIEWED).count();
                return new ParentChildPerformanceDTO(child.getId(), child.getUsername(), attempts.size(), averageScore, (long) assignments.size(), completedAssignments);
        }

        @Override
        public List<ParentAssessmentDTO> getChildAssessments(String parentUsername, UUID childId) {
                Account child = getOwnedChild(parentUsername, childId);
                List<ParentAssessmentDTO> items = new ArrayList<>();
                for (QuizAttempt attempt : quizAttemptRepository.findByStudentIdOrderByStartedAtDesc(child.getId())) {
                        items.add(new ParentAssessmentDTO("QUIZ", attempt.getQuiz() == null ? null : attempt.getQuiz().getTitle(), attempt.getScore(), attempt.getStatus().name(), attempt.getSubmittedAt() != null ? attempt.getSubmittedAt() : attempt.getStartedAt()));
                }
                for (Assignment assignment : assignmentRepository.findByStudentIdOrderByIdDesc(child.getId())) {
                        items.add(new ParentAssessmentDTO("ASSIGNMENT", assignment.getTitle(), null, assignment.getStatus().name(), assignment.getDueAt()));
                }
                items.sort((left, right) -> {
                        Instant leftDate = left.getDate();
                        Instant rightDate = right.getDate();
                        if (leftDate == null && rightDate == null) return 0;
                        if (leftDate == null) return 1;
                        if (rightDate == null) return -1;
                        return rightDate.compareTo(leftDate);
                });
                return items;
        }

        private Account getParentByUsername(String parentUsername) {
                Account parent = accountRepository.findByUsername(parentUsername)
                                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Parent account not found"));
                if (parent.getRole() != AccountRole.ROLE_PARENT) {
                        throw new CustomExceptions.BadRequestException("Account is not a parent");
                }
                return parent;
        }

        private Account getOwnedChild(String parentUsername, UUID childId) {
                Account parent = getParentByUsername(parentUsername);
                ParentStudentLink link = parentStudentLinkRepository.findByParentIdAndStudentId(parent.getId(), childId)
                                .orElseThrow(() -> new CustomExceptions.BadRequestException("Child does not belong to this parent"));
                return link.getStudent();
        }
}
