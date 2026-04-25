package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.entity.StudyClass;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudyClassService {
    StudyClass create(StudyClass studyClass);
    Optional<StudyClass> findById(UUID id);
    List<StudyClass> findAll();
    List<StudyClass> findByTeacherId(UUID teacherId);
    List<StudyClass> findByGradeLevel(Integer gradeLevel);
    StudyClass update(UUID id, StudyClass studyClass);
    void delete(UUID id);
}
