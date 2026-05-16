package com.example.chemlearn.gamification.repository;

import com.example.chemlearn.gamification.entity.MapIsland;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MapIslandRepository extends JpaRepository<MapIsland, UUID> {
    List<MapIsland> findAllByOrderByOrderIndexAsc();
}
