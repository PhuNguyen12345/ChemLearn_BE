package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.entity.StudyClassEnrollment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudyClassEnrollmentService {
    StudyClassEnrollment create(StudyClassEnrollment enrollment);
    Optional<StudyClassEnrollment> findById(UUID id);
    List<StudyClassEnrollment> findAll();
    List<StudyClassEnrollment> findByClassId(UUID classId);
    List<StudyClassEnrollment> findByStudentId(UUID studentId);
    Optional<StudyClassEnrollment> findByClassIdAndStudentId(UUID classId, UUID studentId);
    StudyClassEnrollment update(UUID id, StudyClassEnrollment enrollment);
    void delete(UUID id);
}
