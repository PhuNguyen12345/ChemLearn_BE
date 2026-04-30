package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.lms.entity.Quiz;
import com.example.chemlearn.lms.entity.StudyClass;
import com.example.chemlearn.lms.entity.StudyClassAssignment;
import com.example.chemlearn.lms.repository.StudyClassAssignmentRepository;
import com.example.chemlearn.lms.repository.QuizRepository;
import com.example.chemlearn.lms.repository.StudyClassRepository;
import com.example.chemlearn.lms.repository.UserRepository;
import com.example.chemlearn.lms.service.StudyClassAssignmentService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudyClassAssignmentServiceImpl implements StudyClassAssignmentService {
    @Autowired
    private StudyClassAssignmentRepository assignmentRepository;

    @Autowired
    private StudyClassRepository studyClassRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public StudyClassAssignment create(StudyClassAssignment assignment, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        validateOwnership(assignment, teacherId);
        assignment.setCreatedAt(Instant.now());
        return assignmentRepository.save(assignment);
    }

    @Override
    public Optional<StudyClassAssignment> findById(UUID id, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        return assignmentRepository.findById(id).filter(item -> isOwnedByTeacher(item, teacherId));
    }

    @Override
    public List<StudyClassAssignment> findAll(String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        return assignmentRepository.findAll().stream()
                .filter(item -> isOwnedByTeacher(item, teacherId))
                .toList();
    }

    @Override
    public List<StudyClassAssignment> findByClassId(UUID classId, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        requireOwnedClass(classId, teacherId);
        return assignmentRepository.findByStudyClassField_Id(classId);
    }

    @Override
    public List<StudyClassAssignment> findByQuizId(UUID quizId, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        requireOwnedQuiz(quizId, teacherId);
        return assignmentRepository.findByQuizId(quizId);
    }

    @Override
    public List<StudyClassAssignment> findByLabId(UUID labId, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        return assignmentRepository.findByLabId(labId).stream()
                .filter(item -> isOwnedByTeacher(item, teacherId))
                .toList();
    }

    @Override
    public StudyClassAssignment update(UUID id, StudyClassAssignment assignment, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        StudyClassAssignment existing = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("StudyClassAssignment not found"));
        if (!isOwnedByTeacher(existing, teacherId)) {
            throw new RuntimeException("You are not allowed to modify this assignment");
        }
        validateOwnership(assignment, teacherId);
        if (assignment.getStudyClassField() != null) existing.setStudyClassField(assignment.getStudyClassField());
        if (assignment.getTitle() != null) existing.setTitle(assignment.getTitle());
        if (assignment.getLab() != null) existing.setLab(assignment.getLab());
        if (assignment.getQuiz() != null) existing.setQuiz(assignment.getQuiz());
        if (assignment.getDueDate() != null) existing.setDueDate(assignment.getDueDate());
        return assignmentRepository.save(existing);
    }

    @Override
    public void delete(UUID id, String teacherUsername) {
        UUID teacherId = requireTeacherUser(teacherUsername).getId();
        StudyClassAssignment existing = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("StudyClassAssignment not found"));
        if (!isOwnedByTeacher(existing, teacherId)) {
            throw new RuntimeException("You are not allowed to delete this assignment");
        }
        assignmentRepository.delete(existing);
    }

    private void validateOwnership(StudyClassAssignment assignment, UUID teacherId) {
        StudyClass studyClass = assignment.getStudyClassField();
        if (studyClass == null || studyClass.getId() == null) {
            throw new RuntimeException("Class is required");
        }
        requireOwnedClass(studyClass.getId(), teacherId);

        if (assignment.getQuiz() != null && assignment.getQuiz().getId() != null) {
            requireOwnedQuiz(assignment.getQuiz().getId(), teacherId);
        }
    }

    private boolean isOwnedByTeacher(StudyClassAssignment assignment, UUID teacherId) {
        StudyClass studyClass = assignment.getStudyClassField();
        return studyClass != null
                && studyClass.getTeacher() != null
                && teacherId.equals(studyClass.getTeacher().getId());
    }

    private User requireTeacherUser(String teacherUsername) {
        User user = userRepository.findByUsername(teacherUsername)
                .orElseThrow(() -> new RuntimeException("Teacher account not found"));
        if (user.getRole() != UserRole.ROLE_TEACHER) {
            throw new RuntimeException("Current account is not a teacher");
        }
        return user;
    }

    private StudyClass requireOwnedClass(UUID classId, UUID teacherId) {
        StudyClass studyClass = studyClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        UUID ownerId = studyClass.getTeacher() == null ? null : studyClass.getTeacher().getId();
        if (ownerId == null || !ownerId.equals(teacherId)) {
            throw new RuntimeException("You are not allowed to modify this class");
        }
        return studyClass;
    }

    private Quiz requireOwnedQuiz(UUID quizId, UUID teacherId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        UUID ownerId = quiz.getCreatedBy() == null ? null : quiz.getCreatedBy().getId();
        if (ownerId == null || !ownerId.equals(teacherId)) {
            throw new RuntimeException("You are not allowed to modify this quiz");
        }
        return quiz;
    }
}
