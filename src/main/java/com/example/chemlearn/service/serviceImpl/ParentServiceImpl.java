package com.example.chemlearn.service.serviceImpl;

import com.example.chemlearn.dtos.parent.ParentAssessmentDTO;
import com.example.chemlearn.dtos.parent.ParentChildDTO;
import com.example.chemlearn.dtos.parent.ParentChildPerformanceDTO;
import com.example.chemlearn.entity.Account;
import com.example.chemlearn.entity.Assignment;
import com.example.chemlearn.entity.ParentStudentLink;
import com.example.chemlearn.entity.QuizAttempt;
import com.example.chemlearn.enums.AccountRole;
import com.example.chemlearn.enums.AssignmentStatus;
import com.example.chemlearn.exception.CustomExceptions;
import com.example.chemlearn.repository.AccountRepository;
import com.example.chemlearn.repository.AssignmentRepository;
import com.example.chemlearn.repository.ParentStudentLinkRepository;
import com.example.chemlearn.repository.QuizAttemptRepository;
import com.example.chemlearn.service.ParentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
        return parentStudentLinkRepository.findByParentId(parent.getId())
                .stream()
                .map(link -> new ParentChildDTO(
                        link.getStudent().getId(),
                        link.getStudent().getUsername(),
                        link.getStudent().getEmail()
                ))
                .sorted(Comparator.comparing(ParentChildDTO::getUsername))
                .toList();
    }

    @Override
    public ParentChildPerformanceDTO getChildPerformance(String parentUsername, Long childId) {
        Account child = getOwnedChild(parentUsername, childId);

        List<QuizAttempt> attempts = quizAttemptRepository.findByStudentIdOrderByStartedAtDesc(child.getId());
        List<Assignment> assignments = assignmentRepository.findByStudentIdOrderByIdDesc(child.getId());

        int averageScore = attempts.isEmpty()
                ? 0
                : (int) Math.round(attempts.stream().mapToInt(QuizAttempt::getScore).average().orElse(0));

        long completedAssignments = assignments.stream()
                .filter(a -> a.getStatus() == AssignmentStatus.SUBMITTED || a.getStatus() == AssignmentStatus.REVIEWED)
                .count();

        return new ParentChildPerformanceDTO(
                child.getId(),
                child.getUsername(),
                attempts.size(),
                averageScore,
                (long) assignments.size(),
                completedAssignments
        );
    }

    @Override
    public List<ParentAssessmentDTO> getChildAssessments(String parentUsername, Long childId) {
        Account child = getOwnedChild(parentUsername, childId);

        List<ParentAssessmentDTO> items = new ArrayList<>();

        for (QuizAttempt attempt : quizAttemptRepository.findByStudentIdOrderByStartedAtDesc(child.getId())) {
            items.add(new ParentAssessmentDTO(
                    "QUIZ",
                    attempt.getQuiz().getTitle(),
                    attempt.getScore(),
                    attempt.getStatus().name(),
                    attempt.getSubmittedAt() != null ? attempt.getSubmittedAt() : attempt.getStartedAt()
            ));
        }

        for (Assignment assignment : assignmentRepository.findByStudentIdOrderByIdDesc(child.getId())) {
            items.add(new ParentAssessmentDTO(
                    "ASSIGNMENT",
                    assignment.getTitle(),
                    null,
                    assignment.getStatus().name(),
                    assignment.getDueAt()
            ));
        }

        items.sort((a, b) -> {
            if (a.getDate() == null && b.getDate() == null) return 0;
            if (a.getDate() == null) return 1;
            if (b.getDate() == null) return -1;
            return b.getDate().compareTo(a.getDate());
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

    private Account getOwnedChild(String parentUsername, Long childId) {
        Account parent = getParentByUsername(parentUsername);
        ParentStudentLink link = parentStudentLinkRepository
                .findByParentIdAndStudentId(parent.getId(), childId)
                .orElseThrow(() -> new CustomExceptions.BadRequestException("Child does not belong to this parent"));

        return link.getStudent();
    }
}
