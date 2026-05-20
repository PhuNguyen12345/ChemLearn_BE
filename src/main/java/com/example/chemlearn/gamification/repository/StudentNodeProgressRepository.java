package com.example.chemlearn.gamification.repository;

import com.example.chemlearn.gamification.entity.StudentNodeProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentNodeProgressRepository extends JpaRepository<StudentNodeProgress, UUID> {
    List<StudentNodeProgress> findByStudentId(UUID studentId);
    Optional<StudentNodeProgress> findByStudentIdAndNodeId(UUID studentId, UUID nodeId);
}
