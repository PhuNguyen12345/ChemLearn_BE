package com.example.chemlearn.gamification.repository;

import com.example.chemlearn.gamification.entity.StudentPet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentPetRepository extends JpaRepository<StudentPet, UUID> {
    List<StudentPet> findByStudentId(UUID studentId);
    Optional<StudentPet> findByStudentIdAndSpeciesId(UUID studentId, UUID speciesId);
}
