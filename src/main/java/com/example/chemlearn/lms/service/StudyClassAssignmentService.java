package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.entity.StudyClassAssignment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudyClassAssignmentService {
    StudyClassAssignment create(StudyClassAssignment assignment, String teacherUsername);
    Optional<StudyClassAssignment> findById(UUID id, String teacherUsername);
    List<StudyClassAssignment> findAll(String teacherUsername);
    List<StudyClassAssignment> findByClassId(UUID classId, String teacherUsername);
    List<StudyClassAssignment> findByQuizId(UUID quizId, String teacherUsername);
    List<StudyClassAssignment> findByLabId(UUID labId, String teacherUsername);
    StudyClassAssignment update(UUID id, StudyClassAssignment assignment, String teacherUsername);
    void delete(UUID id, String teacherUsername);
}
