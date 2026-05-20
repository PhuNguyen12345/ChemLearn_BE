package com.example.chemlearn.pvp.dto;

import lombok.Data;

import java.util.UUID;

/**
 * Client → Server: Submit an answer during active turn.
 * Sent via STOMP to /app/battle/action
 */
@Data
public class BattleActionRequest {
    private String roomId;
    /** The answer option selected: "A", "B", "C", or "D" */
    private String selectedOption;
    /** ID of the QuestionBankItem being answered */
    private UUID questionId;
}
