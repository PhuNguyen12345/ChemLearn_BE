package com.example.chemlearn.lms.repository;

import com.example.chemlearn.lms.entity.QuestionBankItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
public interface QuestionBankItemRepository extends JpaRepository<QuestionBankItem, UUID> {
    List<QuestionBankItem> findByCreatedByIdOrderByCreatedAtDesc(UUID teacherId);
    Optional<QuestionBankItem> findByIdAndCreatedById(UUID id, UUID teacherId);
}
