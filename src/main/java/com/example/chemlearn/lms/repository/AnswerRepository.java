package com.example.chemlearn.lms.repository;

import com.example.chemlearn.lms.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
@Repository
public interface AnswerRepository extends JpaRepository<Answer, UUID> {
    List<Answer> findByQuestionIdOrderByOrderIndex(UUID questionId);
    List<Answer> findByQuestionIdAndIsCorrect(UUID questionId, Boolean isCorrect);
}
