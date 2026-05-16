package com.example.chemlearn.lms.repository;

import com.example.chemlearn.lms.entity.RankingHistory;
import com.example.chemlearn.lms.enums.LeaderboardCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface RankingHistoryRepository extends JpaRepository<RankingHistory, UUID> {
    Optional<RankingHistory> findByStudentIdAndCategoryAndRecordDate(UUID studentId, LeaderboardCategory category, LocalDate recordDate);
}
