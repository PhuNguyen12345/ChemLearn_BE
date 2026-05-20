package com.example.chemlearn.lms.repository;

import com.example.chemlearn.lms.entity.StudyClassEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
@Repository

public interface StudyClassEnrollmentRepository extends JpaRepository<StudyClassEnrollment, UUID> {
    List<StudyClassEnrollment> findByStudyClassField_Id(UUID classId);
    List<StudyClassEnrollment> findByStudentId(UUID studentId);
    Optional<StudyClassEnrollment> findByStudyClassField_IdAndStudentId(UUID classId, UUID studentId);

    @Query("SELECT COUNT(DISTINCT e.student.id) FROM StudyClassEnrollment e WHERE e.studyClassField.teacher.id = :teacherId")
    Long countDistinctStudentsByTeacherId(@Param("teacherId") UUID teacherId);
}
