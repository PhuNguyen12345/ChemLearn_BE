package com.example.chemlearn.lms.repository;

import com.example.chemlearn.lms.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, UUID> {
    List<LessonProgress> findByStudentId(UUID studentId);
    Optional<LessonProgress> findByStudentIdAndLessonId(UUID studentId, UUID lessonId);
    List<LessonProgress> findByStudentIdAndIsCompleted(UUID studentId, Boolean isCompleted);
    List<LessonProgress> findByStudentIdAndIsLocked(UUID studentId, Boolean isLocked);
}
