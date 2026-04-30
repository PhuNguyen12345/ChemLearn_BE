package com.example.chemlearn.lms.repository;

import com.example.chemlearn.lms.entity.StudyClassAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudyClassAssignmentRepository extends JpaRepository<StudyClassAssignment, UUID> {
    List<StudyClassAssignment> findByStudyClassField_Id(UUID classId);
    List<StudyClassAssignment> findByQuizId(UUID quizId);
    List<StudyClassAssignment> findByLabId(UUID labId);

    @Query("SELECT CASE WHEN COUNT(sca) > 0 THEN TRUE ELSE FALSE END " +
           "FROM StudyClassAssignment sca " +
           "JOIN StudyClassEnrollment sce ON sce.studyClassField = sca.studyClassField " +
           "WHERE sca.lab.id = :labId AND sce.student.id = :studentId")
    boolean isAssignment(@Param("labId") UUID labId, @Param("studentId") UUID studentId);
}
