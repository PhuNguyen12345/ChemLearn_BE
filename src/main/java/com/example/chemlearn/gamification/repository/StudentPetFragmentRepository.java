package com.example.chemlearn.gamification.repository;

import com.example.chemlearn.gamification.entity.StudentPetFragment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentPetFragmentRepository extends JpaRepository<StudentPetFragment, UUID> {
    List<StudentPetFragment> findByStudentId(UUID studentId);
    Optional<StudentPetFragment> findByStudentIdAndSpeciesId(UUID studentId, UUID speciesId);
}
