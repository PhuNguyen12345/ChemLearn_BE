package com.example.chemlearn.lab.repository;

import com.example.chemlearn.lab.entity.UserLabProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserLabProgressRepository extends JpaRepository<UserLabProgress, UUID> {
    Optional<UserLabProgress> findByStudentIdAndLabId(UUID studentId, UUID labId);
    void deleteByStudentIdAndLabId(UUID studentId, UUID labId);
}
