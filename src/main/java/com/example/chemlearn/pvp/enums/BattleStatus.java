package com.example.chemlearn.pvp.enums;

public enum BattleStatus {
    WAITING,        // Room created, waiting for battle to start
    IN_PROGRESS,    // Battle is actively ongoing
    PLAYER1_WON,    // Player 1 won
    PLAYER2_WON,    // Player 2 won
    DRAW            // Both players disconnected or timed out simultaneously
}
