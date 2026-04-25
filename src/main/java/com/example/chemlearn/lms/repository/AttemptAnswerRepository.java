package com.example.chemlearn.lms.repository;

import com.example.chemlearn.lms.entity.AttemptAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AttemptAnswerRepository extends JpaRepository<AttemptAnswer, UUID> {
}
