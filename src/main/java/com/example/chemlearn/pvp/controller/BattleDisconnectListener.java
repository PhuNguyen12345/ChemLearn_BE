package com.example.chemlearn.pvp.controller;

import com.example.chemlearn.pvp.model.BattleRoom;
import com.example.chemlearn.pvp.model.PlayerSlot;
import com.example.chemlearn.pvp.service.BattleLifecycleService;
import com.example.chemlearn.pvp.service.BattleRoomStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Optional;

/**
 * Listens for WebSocket session disconnection events.
 * When a player disconnects mid-battle, the opponent wins automatically.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BattleDisconnectListener {

    private final BattleRoomStore battleRoomStore;
    private final BattleLifecycleService battleLifecycleService;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        log.info("WebSocket session disconnected: {}", sessionId);

        Optional<BattleRoom> roomOpt = battleRoomStore.findBySessionId(sessionId);
        if (roomOpt.isEmpty()) {
            log.debug("Disconnected session {} was not in any active battle room", sessionId);
            return;
        }

        BattleRoom room = roomOpt.get();

        // Identify which player disconnected
        PlayerSlot disconnectedPlayer = null;
        PlayerSlot opponent = null;

        if (sessionId.equals(room.getPlayer1().getWsSessionId())) {
            disconnectedPlayer = room.getPlayer1();
            opponent = room.getPlayer2();
        } else if (sessionId.equals(room.getPlayer2().getWsSessionId())) {
            disconnectedPlayer = room.getPlayer2();
            opponent = room.getPlayer1();
        }

        if (disconnectedPlayer == null || opponent == null) return;

        disconnectedPlayer.markDisconnected();

        log.warn("[Room {}] Player {} disconnected. {} wins by forfeit.",
                room.getRoomId(), disconnectedPlayer.getUsername(), opponent.getUsername());

        // Opponent wins by disconnect
        battleLifecycleService.endGame(room, opponent.getStudentId(), "DISCONNECT");
    }
}
