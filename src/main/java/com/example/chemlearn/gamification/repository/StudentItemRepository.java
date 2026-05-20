package com.example.chemlearn.gamification.repository;

import com.example.chemlearn.gamification.entity.StudentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentItemRepository extends JpaRepository<StudentItem, UUID> {
    List<StudentItem> findByStudentId(UUID studentId);
    Optional<StudentItem> findByStudentIdAndItemId(UUID studentId, UUID itemId);
}
