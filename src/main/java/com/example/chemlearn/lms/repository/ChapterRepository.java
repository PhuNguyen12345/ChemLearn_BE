package com.example.chemlearn.lms.repository;

import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.lms.entity.Chapter;
import com.example.chemlearn.lms.enums.MaterialScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChapterRepository extends JpaRepository<Chapter, UUID> {
    List<Chapter> findByPublishedTrueAndMaterialScopeOrderByOrderIndexAsc(MaterialScope materialScope);

    @Query("""
            select c
            from Chapter c
            where c.createdBy is not null
                and (
                    (c.createdBy.role = :adminRole and c.materialScope = :globalScope)
                    or c.createdBy.id = :teacherId
                )
            order by c.orderIndex asc
            """)
    List<Chapter> findVisibleToTeacher(@Param("teacherId") UUID teacherId,
                                       @Param("adminRole") UserRole adminRole,
                                       @Param("globalScope") MaterialScope globalScope);

    Optional<Chapter> findByIdAndCreatedBy_Id(UUID chapterId, UUID createdById);

    List<Chapter> findByMaterialScopeOrderByOrderIndexAsc(MaterialScope materialScope);
}
