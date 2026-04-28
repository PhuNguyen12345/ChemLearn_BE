package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.lms.entity.StudyClassAssignment;
import com.example.chemlearn.lms.repository.StudyClassAssignmentRepository;
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

    @Override
    public StudyClassAssignment create(StudyClassAssignment assignment) {
        assignment.setCreatedAt(Instant.now());
        return assignmentRepository.save(assignment);
    }

    @Override
    public Optional<StudyClassAssignment> findById(UUID id) {
        return assignmentRepository.findById(id);
    }

    @Override
    public List<StudyClassAssignment> findAll() {
        return assignmentRepository.findAll();
    }

    @Override
    public List<StudyClassAssignment> findByClassId(UUID classId) {
        return assignmentRepository.findByStudyClassField_Id(classId);
    }

    @Override
    public List<StudyClassAssignment> findByQuizId(UUID quizId) {
        return assignmentRepository.findByQuizId(quizId);
    }

    @Override
    public List<StudyClassAssignment> findByLabId(UUID labId) {
        return assignmentRepository.findByLabId(labId);
    }

    @Override
    public StudyClassAssignment update(UUID id, StudyClassAssignment assignment) {
        StudyClassAssignment existing = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("StudyClassAssignment not found"));
        if (assignment.getStudyClassField() != null) existing.setStudyClassField(assignment.getStudyClassField());
        if (assignment.getTitle() != null) existing.setTitle(assignment.getTitle());
        if (assignment.getLab() != null) existing.setLab(assignment.getLab());
        if (assignment.getQuiz() != null) existing.setQuiz(assignment.getQuiz());
        if (assignment.getDueDate() != null) existing.setDueDate(assignment.getDueDate());
        return assignmentRepository.save(existing);
    }

    @Override
    public void delete(UUID id) {
        assignmentRepository.deleteById(id);
    }
}
