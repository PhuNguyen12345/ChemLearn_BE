package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.lms.entity.StudyClass;
import com.example.chemlearn.lms.repository.StudyClassRepository;
import com.example.chemlearn.lms.service.StudyClassService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyClassServiceImpl implements StudyClassService {
    private final StudyClassRepository studyClassRepository;

    @Override
    public StudyClass create(StudyClass studyClass) {
        Instant now = Instant.now();
        studyClass.setCreatedAt(now);
        studyClass.setUpdatedAt(now);
        return studyClassRepository.save(studyClass);
    }

    @Override
    public Optional<StudyClass> findById(UUID id) {
        return studyClassRepository.findById(id);
    }

    @Override
    public List<StudyClass> findAll() {
        return studyClassRepository.findAll();
    }

    @Override
    public List<StudyClass> findByTeacherId(UUID teacherId) {
        return studyClassRepository.findByTeacherId(teacherId);
    }

    @Override
    public List<StudyClass> findByGradeLevel(Integer gradeLevel) {
        return studyClassRepository.findByGradeLevel(gradeLevel);
    }

    @Override
    public StudyClass update(UUID id, StudyClass studyClass) {
        StudyClass existing = studyClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("StudyClass not found"));
        if (studyClass.getName() != null) existing.setName(studyClass.getName());
        if (studyClass.getGradeLevel() != null) existing.setGradeLevel(studyClass.getGradeLevel());
        if (studyClass.getTeacher() != null) existing.setTeacher(studyClass.getTeacher());
        existing.setUpdatedAt(Instant.now());
        return studyClassRepository.save(existing);
    }

    @Override
    public void delete(UUID id) {
        studyClassRepository.deleteById(id);
    }
}
