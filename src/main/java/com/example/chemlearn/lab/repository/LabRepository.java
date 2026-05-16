package com.example.chemlearn.lab.repository;

import com.example.chemlearn.lab.entity.Lab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LabRepository extends JpaRepository<Lab, UUID> {
}
