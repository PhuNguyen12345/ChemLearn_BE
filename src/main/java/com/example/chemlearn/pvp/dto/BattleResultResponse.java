package com.example.chemlearn.pvp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Server → Client: Final result sent when the battle ends.
 * Sent to /topic/battle/{roomId}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleResultResponse {
    private String roomId;
    private String winnerId;
    private String winnerName;
    private String loserId;
    private String loserName;

    private int winnerXpGained;
    private int winnerCoinsGained;
    private int loserXpGained;   // Consolation XP for the loser
    private int loserCoinsGained;

    private String endReason; // "HP_ZERO", "DISCONNECT", "TIMEOUT"
}
