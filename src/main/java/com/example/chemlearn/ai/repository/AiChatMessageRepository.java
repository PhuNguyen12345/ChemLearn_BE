package com.example.chemlearn.ai.repository;

import com.example.chemlearn.ai.entity.AiChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, UUID> {
    List<AiChatMessage> findBySession_IdOrderByCreatedAtAsc(UUID sessionId);
}
