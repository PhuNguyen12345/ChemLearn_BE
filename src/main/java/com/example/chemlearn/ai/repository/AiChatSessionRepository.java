package com.example.chemlearn.ai.repository;

import com.example.chemlearn.ai.entity.AiChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiChatSessionRepository extends JpaRepository<AiChatSession, UUID> {
    List<AiChatSession> findByStudent_IdOrderByUpdatedAtDesc(UUID studentId);

    Optional<AiChatSession> findByIdAndStudent_Id(UUID sessionId, UUID studentId);
}
