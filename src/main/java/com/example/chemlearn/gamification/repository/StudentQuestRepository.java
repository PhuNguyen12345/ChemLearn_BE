package com.example.chemlearn.gamification.repository;

import com.example.chemlearn.gamification.entity.StudentQuest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentQuestRepository extends JpaRepository<StudentQuest, UUID> {
    List<StudentQuest> findByStudentIdAndIsClaimedFalse(UUID studentId);
    List<StudentQuest> findByStudentIdAndAssignedDate(UUID studentId, LocalDate assignedDate);
    Optional<StudentQuest> findByStudentIdAndQuestIdAndAssignedDate(UUID studentId, UUID questId, LocalDate assignedDate);
}
