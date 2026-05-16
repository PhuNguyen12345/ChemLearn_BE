package com.example.chemlearn.lms.repository;

import com.example.chemlearn.lms.entity.StudyClassAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
@Repository
public interface StudyClassAssignmentRepository extends JpaRepository<StudyClassAssignment, UUID> {
    List<StudyClassAssignment> findByStudyClassField_Id(UUID classId);
    List<StudyClassAssignment> findByQuizId(UUID quizId);
    List<StudyClassAssignment> findByLabId(UUID labId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(a) FROM StudyClassAssignment a JOIN a.studyClassField c JOIN c.enrollments e WHERE e.student.id = :studentId")
    Long countAssignmentsByStudentId(@org.springframework.data.repository.query.Param("studentId") UUID studentId);
}
