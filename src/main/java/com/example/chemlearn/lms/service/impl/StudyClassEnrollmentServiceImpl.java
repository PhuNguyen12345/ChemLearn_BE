package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.lms.entity.StudyClassEnrollment;
import com.example.chemlearn.lms.repository.StudyClassEnrollmentRepository;
import com.example.chemlearn.lms.service.StudyClassEnrollmentService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudyClassEnrollmentServiceImpl implements StudyClassEnrollmentService {
    @Autowired
    private StudyClassEnrollmentRepository enrollmentRepository;

    @Override
    public StudyClassEnrollment create(StudyClassEnrollment enrollment) {
        enrollment.setJoinedAt(Instant.now());
        return enrollmentRepository.save(enrollment);
    }

    @Override
    public Optional<StudyClassEnrollment> findById(UUID id) {
        return enrollmentRepository.findById(id);
    }

    @Override
    public List<StudyClassEnrollment> findAll() {
        return enrollmentRepository.findAll();
    }

    @Override
    public List<StudyClassEnrollment> findByClassId(UUID classId) {
        return enrollmentRepository.findByStudyClassField_Id(classId);
    }

    @Override
    public List<StudyClassEnrollment> findByStudentId(UUID studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    @Override
    public Optional<StudyClassEnrollment> findByClassIdAndStudentId(UUID classId, UUID studentId) {
        return enrollmentRepository.findByStudyClassField_IdAndStudentId(classId, studentId);
    }

    @Override
    public StudyClassEnrollment update(UUID id, StudyClassEnrollment enrollment) {
        StudyClassEnrollment existing = enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("StudyClassEnrollment not found"));
        if (enrollment.getStudyClassField() != null) existing.setStudyClassField(enrollment.getStudyClassField());
        if (enrollment.getStudent() != null) existing.setStudent(enrollment.getStudent());
        if (enrollment.getJoinedAt() != null) existing.setJoinedAt(enrollment.getJoinedAt());
        return enrollmentRepository.save(existing);
    }

    @Override
    public void delete(UUID id) {
        enrollmentRepository.deleteById(id);
    }
}
