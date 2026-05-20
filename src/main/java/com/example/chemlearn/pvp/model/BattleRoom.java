package com.example.chemlearn.pvp.model;

import com.example.chemlearn.pvp.dto.QuestionPayload;
import com.example.chemlearn.pvp.enums.BattleStatus;
import lombok.Builder;
import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory state for an active PVP battle room.
 * Thread-safe via AtomicInteger for turn management.
 * Stored in BattleRoomStore (ConcurrentHashMap).
 */
@Data
@Builder
public class BattleRoom {
    private String roomId;

    private PlayerSlot player1;
    private PlayerSlot player2;

    @Builder.Default
    private BattleStatus status = BattleStatus.WAITING;

    // 0 = player1's turn, 1 = player2's turn
    @Builder.Default
    private AtomicInteger currentTurnIndex = new AtomicInteger(0);

    // The current question being presented to the active player
    private volatile QuestionPayload currentQuestion;

    // Unix timestamp millis — turn expires after this
    @Builder.Default
    private volatile long turnDeadline = 0L;

    private static final int TURN_TIMEOUT_SECONDS = 30;

    // --- Convenience accessors ---

    public PlayerSlot getCurrentTurnPlayer() {
        return currentTurnIndex.get() == 0 ? player1 : player2;
    }

    public PlayerSlot getOpponent(String studentId) {
        if (player1.getStudentId().equals(studentId)) return player2;
        return player1;
    }

    public PlayerSlot getPlayer(String studentId) {
        if (player1.getStudentId().equals(studentId)) return player1;
        return player2;
    }

    public void switchTurn() {
        currentTurnIndex.set(currentTurnIndex.get() == 0 ? 1 : 0);
        this.turnDeadline = System.currentTimeMillis() + (TURN_TIMEOUT_SECONDS * 1000L);
    }

    public void resetTurnDeadline() {
        this.turnDeadline = System.currentTimeMillis() + (TURN_TIMEOUT_SECONDS * 1000L);
    }

    public boolean isTurnExpired() {
        return turnDeadline > 0 && System.currentTimeMillis() > turnDeadline;
    }

    public boolean isCurrentTurnPlayer(String studentId) {
        return getCurrentTurnPlayer().getStudentId().equals(studentId);
    }

    public boolean isCurrentTurnPlayerByUsername(String username) {
        return getCurrentTurnPlayer().getUsername().equals(username);
    }

    public PlayerSlot getOpponentByUsername(String username) {
        if (player1.getUsername().equals(username)) return player2;
        return player1;
    }
}
