package com.example.chemlearn.pvp.controller;

import com.example.chemlearn.pvp.dto.BattleActionRequest;
import com.example.chemlearn.pvp.dto.JoinQueueRequest;
import com.example.chemlearn.pvp.service.CombatService;
import com.example.chemlearn.pvp.service.MatchmakingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * STOMP WebSocket controller for PVP battle interactions.
 * All methods are triggered by STOMP messages from the client.
 *
 * Client connection: ws://localhost:8080/ws (with SockJS fallback)
 * Subscriptions: /topic/battle/{roomId} and /topic/battle/{roomId}/result
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class BattleController {

    private final MatchmakingService matchmakingService;
    private final CombatService combatService;

    /**
     * Endpoint: /app/battle/join
     * Payload: JoinQueueRequest { studentPetId }
     *
     * The authenticated student (Principal) joins the matchmaking queue.
     * If another player is already waiting, a battle room is created automatically.
     */
    @MessageMapping("/battle/join")
    public void joinQueue(JoinQueueRequest request,
                          Principal principal,
                          SimpMessageHeaderAccessor headerAccessor) {
        if (principal == null) {
            log.warn("Unauthenticated WebSocket join attempt rejected");
            return;
        }


        String username = principal.getName();
        String wsSessionId = headerAccessor.getSessionId();

        log.info("Player {} joining queue with pet {}", username, request.getStudentPetId());

        matchmakingService.joinQueue(username, request.getStudentPetId(), username, wsSessionId);
    }

    /**
     * Endpoint: /app/battle/action
     * Payload: BattleActionRequest { roomId, selectedOption, questionId }
     *
     * The student submits their answer for the current turn question.
     * CombatService resolves the answer and broadcasts the updated game state.
     */
    @MessageMapping("/battle/action")
    public void submitAction(BattleActionRequest request, Principal principal) {
        if (principal == null) {
            log.warn("Unauthenticated action rejected");
            return;
        }

        String username = principal.getName();
        log.info("Player {} submitted action in room {}: option={}",
                username, request.getRoomId(), request.getSelectedOption());

        combatService.resolveAction(request, username);
    }

    @MessageMapping("/battle/leave")
    public void leaveQueue(Principal principal) {
        if (principal == null) return;
        String username = principal.getName();
        log.info("Player {} leaving queue", username);
        matchmakingService.leaveQueue(username);
    }
}
