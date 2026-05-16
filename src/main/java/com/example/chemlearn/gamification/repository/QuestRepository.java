package com.example.chemlearn.gamification.repository;

import com.example.chemlearn.gamification.entity.Quest;
import com.example.chemlearn.gamification.enums.QuestCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestRepository extends JpaRepository<Quest, UUID> {
    List<Quest> findByCategoryAndIsActiveTrue(QuestCategory category);
}
