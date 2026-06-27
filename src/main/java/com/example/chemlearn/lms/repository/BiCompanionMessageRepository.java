package com.example.chemlearn.lms.repository;

import com.example.chemlearn.lms.entity.BiCompanionMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BiCompanionMessageRepository extends JpaRepository<BiCompanionMessage, UUID> {
    List<BiCompanionMessage> findTop30ByStudent_IdOrderByCreatedAtDesc(UUID studentId);

    boolean existsByStudent_IdAndMessageTypeAndScheduledFor(
            UUID studentId,
            String messageType,
            LocalDate scheduledFor
    );
}
