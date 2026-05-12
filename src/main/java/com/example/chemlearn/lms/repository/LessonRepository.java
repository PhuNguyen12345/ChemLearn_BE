package com.example.chemlearn.lms.repository;

import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.lms.entity.Lesson;
import com.example.chemlearn.lms.enums.MaterialScope;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    List<Lesson> findByChapterIdAndPublishedTrueOrderByOrderIndexAsc(UUID chapterId);
    Optional<Lesson> findByIdAndPublishedTrue(UUID id);
    List<Lesson> findByChapterIdOrderByOrderIndexAsc(UUID chapterId);
    int countByChapterId(UUID chapterId);

    List<Lesson> findByChapterIdAndPublishedTrueAndCreatedBy_RoleOrderByOrderIndexAsc(UUID chapterId, UserRole role);

    List<Lesson> findByChapterIdAndPublishedTrueAndMaterialScopeOrderByOrderIndexAsc(UUID chapterId,
                                                                                                        MaterialScope materialScope);

    Optional<Lesson> findByIdAndPublishedTrueAndCreatedBy_Role(UUID id, UserRole role);

    Optional<Lesson> findByIdAndPublishedTrueAndMaterialScope(UUID id, MaterialScope materialScope);

    List<Lesson> findByCreatedBy_IdOrderByOrderIndexAsc(UUID createdById);

    Optional<Lesson> findByIdAndCreatedBy_Id(UUID lessonId, UUID createdById);
}
