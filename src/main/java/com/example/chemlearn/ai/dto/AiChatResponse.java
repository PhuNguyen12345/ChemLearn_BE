package com.example.chemlearn.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {
    private UUID sessionId;
    private String topic;
    private String answer;
    private List<SuggestedLabDTO> suggestedLabs;
}
