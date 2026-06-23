package com.example.chemlearn.payment.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.chemlearn.payment.entity.LearningPackage;

@Repository
public interface LearningPackageRepository extends JpaRepository<LearningPackage, UUID> {
    Optional<LearningPackage> findByPackageCode(String packageCode);

    List<LearningPackage> findAllByIsActiveTrueOrderByGradeLevelAsc();
}