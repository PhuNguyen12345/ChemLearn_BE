package com.example.chemlearn.lms.repository;

import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.lms.entity.Lesson;
import com.example.chemlearn.lms.enums.MaterialScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("""
            select l
            from Lesson l
            join fetch l.chapter c
            left join fetch l.lab lab
            where c.gradeLevel = :grade
                and l.published = true
                and c.published = true
                and l.materialScope = :materialScope
                and c.materialScope = :materialScope
            order by c.orderIndex asc, l.orderIndex asc
            """)
    List<Lesson> findPublishedGlobalLessonsByGrade(@Param("grade") Integer grade,
                                                   @Param("materialScope") MaterialScope materialScope);

    @Query("""
            select l
            from Lesson l
            join fetch l.chapter c
            left join fetch l.lab lab
            where c.gradeLevel = :grade
                and l.published = true
                and c.published = true
                and l.materialScope = :materialScope
                and c.materialScope = :materialScope
                and (
                    lower(l.title) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(l.textContent, '')) like lower(concat('%', :keyword, '%'))
                    or lower(c.title) like lower(concat('%', :keyword, '%'))
                )
            order by c.orderIndex asc, l.orderIndex asc
            """)
    List<Lesson> searchPublishedGlobalLessonsByGradeAndKeyword(@Param("grade") Integer grade,
                                                               @Param("materialScope") MaterialScope materialScope,
                                                               @Param("keyword") String keyword);
}
