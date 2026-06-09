package com.example.chemlearn.ai.dto;

import com.example.chemlearn.ai.enums.BookType;
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
public class AiChatSessionResponse {
    private UUID id;
    private Integer grade;
    private BookType bookType;
    private String topic;
    private Instant createdAt;
    private Instant updatedAt;
}
