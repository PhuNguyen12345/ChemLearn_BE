package com.example.chemlearn.gamification.repository;

import com.example.chemlearn.gamification.entity.MapNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MapNodeRepository extends JpaRepository<MapNode, UUID> {
    List<MapNode> findByIslandIdOrderByOrderIndexAsc(UUID islandId);
}
