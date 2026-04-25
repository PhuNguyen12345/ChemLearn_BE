package com.example.chemlearn.lab.repository;

import com.example.chemlearn.lab.entity.Lab;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LabRepository extends JpaRepository<Lab, UUID> {
    Page<Lab> findAll(Pageable pageable);
}
