package com.example.chemlearn.lms.repository;

import com.example.chemlearn.lms.entity.ParentStudentLink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParentStudentLinkRepository extends JpaRepository<ParentStudentLink, UUID> {
    List<ParentStudentLink> findByParentId(UUID parentId);
    Optional<ParentStudentLink> findByParentIdAndStudentId(UUID parentId, UUID studentId);
}
