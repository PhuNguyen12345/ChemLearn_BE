package com.example.chemlearn.lab.repository;

import com.example.chemlearn.lab.entity.Lab;
import com.example.chemlearn.lab.enums.LabCategory;
import com.example.chemlearn.lab.enums.LabType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LabRepository extends JpaRepository<Lab, UUID> {
    Page<Lab> findAll(Pageable pageable);
    Optional<Lab> findByTitle(String title);
    
    // PREMADE for all
    @Query("SELECT l FROM Lab l WHERE l.type = 'PREMADE' AND " +
            "(:keyword IS NULL OR LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:category IS NULL OR l.category = :category)")
    Page<Lab> findPremadeLabs(
            @Param("keyword") String keyword,
            @Param("category") LabCategory category,
            Pageable pageable
    );

    // SANDBOX for particular user
    @Query("SELECT l FROM Lab l WHERE l.type = 'SANDBOX' AND l.authorId = :studentId AND " +
            "(:keyword IS NULL OR LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:category IS NULL OR l.category = :category)")
    Page<Lab> findMySandboxLabs(
            @Param("studentId") UUID studentId,
            @Param("keyword") String keyword,
            @Param("category") LabCategory category,
            Pageable pageable
    );

    // ASSIGNMENT for particular user
    @Query("SELECT DISTINCT l FROM Lab l " +
           "JOIN StudyClassAssignment sca ON sca.lab = l " +
           "JOIN StudyClassEnrollment sce ON sce.studyClassField = sca.studyClassField " +
           "WHERE sce.student.id = :studentId " +
           "AND (:keyword IS NULL OR LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:category IS NULL OR l.category = :category)")
    Page<Lab> findMyAssignmentLabs(
            @Param("studentId") UUID studentId,
            @Param("keyword") String keyword,
            @Param("category") LabCategory category,
            Pageable pageable
    );
}
