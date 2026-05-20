package com.example.chemlearn.pvp.dto;

import com.example.chemlearn.pvp.enums.BattleStatus;
import com.example.chemlearn.pvp.model.PetState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Server → Client: Full state of the battle, broadcast after every action.
 * Sent to /topic/battle/{roomId}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameStateResponse {

    private String roomId;
    private BattleStatus status;

    // Player info
    private String player1Id;
    private String player1Name;
    private PetState player1Pet;

    private String player2Id;
    private String player2Name;
    private PetState player2Pet;

    // Whose turn it is (0 = player1, 1 = player2)
    private int currentTurnIndex;
    private String currentTurnPlayerId;

    // The question for the current turn (sent to all — FE only shows to active player)
    private QuestionPayload currentQuestion;

    // Turn deadline in Unix millis for countdown timer on FE
    private long turnDeadline;

    // Last action result — shown as an event log on FE
    private String lastActionResult; // "CORRECT", "WRONG", "TIMEOUT", "BATTLE_START"
    private int lastDamageDealt;

    // Populated only when status = PLAYER1_WON / PLAYER2_WON
    private String winnerId;
    private String winnerName;
}
