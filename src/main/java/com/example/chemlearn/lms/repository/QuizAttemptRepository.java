package com.example.chemlearn.lms.repository;

import com.example.chemlearn.lms.entity.QuizAttempt;
import com.example.chemlearn.lms.enums.AttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.chemlearn.lms.dto.teacher.ChapterAverageScoreDTO;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {
    Optional<QuizAttempt> findByIdAndStudentId(UUID id, UUID studentId);
    Optional<QuizAttempt> findFirstByQuizIdAndStudentIdAndStatusOrderByStartedAtDesc(UUID quizId, UUID studentId, AttemptStatus status);
    List<QuizAttempt> findByQuizCreatedByIdOrderByStartedAtDesc(UUID teacherId);
    List<QuizAttempt> findByStudentIdOrderByStartedAtDesc(UUID studentId);
    List<QuizAttempt> findByQuizIdAndStudentIdOrderByStartedAtDesc(UUID quizId, UUID studentId);

    @Query("SELECT COUNT(qa) FROM QuizAttempt qa WHERE qa.student.id IN (SELECT e.student.id FROM StudyClassEnrollment e WHERE e.studyClassField.teacher.id = :teacherId) AND FUNCTION('DATE', qa.submittedAt) = CURRENT_DATE")
    Long countSubmissionsTodayByTeacherId(@Param("teacherId") UUID teacherId);

    @Query("SELECT new com.example.chemlearn.lms.dto.teacher.ChapterAverageScoreDTO(c.title, AVG(CAST(qa.score AS double))) " +
           "FROM QuizAttempt qa " +
           "JOIN qa.lesson l " +
           "JOIN l.chapter c " +
           "WHERE qa.student.id IN (SELECT e.student.id FROM StudyClassEnrollment e WHERE e.studyClassField.teacher.id = :teacherId) " +
           "GROUP BY c.id, c.title")
    List<ChapterAverageScoreDTO> getAverageScoreByChapterAndTeacherId(@Param("teacherId") UUID teacherId);

    @Query("SELECT COUNT(qa) FROM QuizAttempt qa WHERE qa.student.id = :studentId AND qa.status = 'COMPLETED'")
    Long countCompletedQuizzesByStudentId(@Param("studentId") UUID studentId);

    List<QuizAttempt> findTop10ByStudentIdAndStatusOrderBySubmittedAtAsc(UUID studentId, AttemptStatus status);
    List<QuizAttempt> findTop5ByStudentIdAndStatusOrderBySubmittedAtDesc(UUID studentId, AttemptStatus status);
}
