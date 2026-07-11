package com.example.chemlearn.lab.repository;

import com.example.chemlearn.lab.entity.LabConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LabConfigurationRepository extends JpaRepository<LabConfiguration, UUID> {
    Optional<LabConfiguration> findByLabId(UUID labId);
}
