package com.example.chemlearn.pvp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Server → Client: A Chemistry question sent to the active player.
 * Embedded in GameStateResponse. Options are shuffled — correct answer is NOT included.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionPayload {
    private UUID questionId;
    private String prompt;

    /** Shuffled display options. Map: "A" -> text, "B" -> text, etc. */
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    /** Stored server-side only; NOT sent to client in this DTO.
     *  The correct answer key is stored in BattleRoom.currentQuestion separately. */
    private transient String correctOption; // transient = not serialized to JSON
}
