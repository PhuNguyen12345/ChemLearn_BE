package com.example.chemlearn.lms.repository;

import com.example.chemlearn.core.entity.Parent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ParentRepository extends JpaRepository<Parent, UUID> {
}
