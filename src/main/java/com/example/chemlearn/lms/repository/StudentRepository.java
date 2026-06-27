package com.example.chemlearn.lms.repository;

import com.example.chemlearn.core.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

import java.util.List;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, UUID> {
    List<Student> findByParentId(UUID parentId);
    Optional<Student> findByUsers_Username(String username);
    Optional<Student> findByUsers_Id(UUID id);

    @Query("select s from Student s join fetch s.users u where u.isActive = true")
    List<Student> findActiveStudentsWithUsers();

    // Leaderboard: Top N
    org.springframework.data.domain.Page<Student> findAllByOrderByExperienceDesc(org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<Student> findAllByOrderByCurrentStreakDesc(org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<Student> findAllByOrderByPvpWinsDesc(org.springframework.data.domain.Pageable pageable);

    // Leaderboard: Rank calculations (count students with score > my score)
    int countByExperienceGreaterThan(Integer experience);
    int countByCurrentStreakGreaterThan(Integer currentStreak);
    int countByPvpWinsGreaterThan(Integer pvpWins);
}
