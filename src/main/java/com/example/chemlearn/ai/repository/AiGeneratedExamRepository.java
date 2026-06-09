package com.example.chemlearn.ai.repository;

import com.example.chemlearn.ai.entity.AiGeneratedExam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiGeneratedExamRepository extends JpaRepository<AiGeneratedExam, UUID> {
    Optional<AiGeneratedExam> findByIdAndStudent_Id(UUID examId, UUID studentId);
    List<AiGeneratedExam> findByStudent_IdOrderByCreatedAtDesc(UUID studentId);
}
