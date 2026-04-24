package com.example.chemlearn.repository;

import com.example.chemlearn.entity.ParentStudentLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParentStudentLinkRepository extends JpaRepository<ParentStudentLink, Long> {
    List<ParentStudentLink> findByParentId(Long parentId);

    Optional<ParentStudentLink> findByParentIdAndStudentId(Long parentId, Long studentId);
}
