package com.example.chemlearn.lms.repository;

import com.example.chemlearn.lms.entity.FeedbackReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeedbackReportRepository extends JpaRepository<FeedbackReport, UUID> {
    List<FeedbackReport> findAllByOrderByCreatedAtDesc();
    long countByStatusNot(String status);
}
