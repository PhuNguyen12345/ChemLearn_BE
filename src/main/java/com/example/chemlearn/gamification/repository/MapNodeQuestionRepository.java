package com.example.chemlearn.gamification.repository;

import com.example.chemlearn.gamification.entity.MapNodeQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MapNodeQuestionRepository extends JpaRepository<MapNodeQuestion, UUID> {
    List<MapNodeQuestion> findByNodeId(UUID nodeId);
}
