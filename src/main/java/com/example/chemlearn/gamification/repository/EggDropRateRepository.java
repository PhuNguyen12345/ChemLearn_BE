package com.example.chemlearn.gamification.repository;

import com.example.chemlearn.gamification.entity.EggDropRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EggDropRateRepository extends JpaRepository<EggDropRate, UUID> {
    List<EggDropRate> findByEggItemId(UUID eggItemId);
}
