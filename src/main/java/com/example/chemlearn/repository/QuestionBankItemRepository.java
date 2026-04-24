package com.example.chemlearn.repository;

import com.example.chemlearn.entity.QuestionBankItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionBankItemRepository extends JpaRepository<QuestionBankItem, Long> {
    List<QuestionBankItem> findByCreatedByIdOrderByCreatedAtDesc(Long teacherId);

    Optional<QuestionBankItem> findByIdAndCreatedById(Long id, Long teacherId);
}
