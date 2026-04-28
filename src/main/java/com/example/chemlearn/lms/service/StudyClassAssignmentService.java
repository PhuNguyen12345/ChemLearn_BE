package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.entity.StudyClassAssignment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudyClassAssignmentService {
    StudyClassAssignment create(StudyClassAssignment assignment);
    Optional<StudyClassAssignment> findById(UUID id);
    List<StudyClassAssignment> findAll();
    List<StudyClassAssignment> findByClassId(UUID classId);
    List<StudyClassAssignment> findByQuizId(UUID quizId);
    List<StudyClassAssignment> findByLabId(UUID labId);
    StudyClassAssignment update(UUID id, StudyClassAssignment assignment);
    void delete(UUID id);
}
