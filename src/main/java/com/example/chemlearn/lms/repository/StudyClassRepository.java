package com.example.chemlearn.lms.repository;

import com.example.chemlearn.lms.entity.StudyClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
@Repository
public interface StudyClassRepository extends JpaRepository<StudyClass, UUID> {
    List<StudyClass> findByTeacherId(UUID teacherId);
    List<StudyClass> findByGradeLevel(Integer gradeLevel);
}
