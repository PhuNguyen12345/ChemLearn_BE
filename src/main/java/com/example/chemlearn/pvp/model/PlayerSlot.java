package com.example.chemlearn.pvp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents one of the two players inside a BattleRoom.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSlot {
    private String studentId;   // UUID as string
    private String username;
    private String displayName;
    private String wsSessionId; // WebSocket STOMP session ID

    private PetState petState;

    @Builder.Default
    private boolean connected = true;

    @Builder.Default
    private long lastHeartbeat = System.currentTimeMillis();

    public void markDisconnected() {
        this.connected = false;
    }
}
