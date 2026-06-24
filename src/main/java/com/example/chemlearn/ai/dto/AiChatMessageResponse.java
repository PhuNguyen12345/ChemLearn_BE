package com.example.chemlearn.ai.dto;

import com.example.chemlearn.ai.enums.AiMessageRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatMessageResponse {
    private UUID id;
    private AiMessageRole role;
    private String content;
    private String speechText;
    private Instant createdAt;
}
