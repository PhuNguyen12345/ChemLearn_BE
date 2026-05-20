package com.example.chemlearn.gamification.repository;

import com.example.chemlearn.gamification.entity.XpLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface XpLogRepository extends JpaRepository<XpLog, UUID> {
}
