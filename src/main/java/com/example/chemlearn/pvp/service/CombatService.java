package com.example.chemlearn.pvp.service;

import com.example.chemlearn.pvp.dto.BattleActionRequest;
import com.example.chemlearn.pvp.enums.BattleStatus;
import com.example.chemlearn.pvp.model.BattleRoom;
import com.example.chemlearn.pvp.model.PetState;
import com.example.chemlearn.pvp.model.PlayerSlot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Core combat resolution service.
 * Checks answers, calculates damage, and triggers turn transitions.
 * All calculations are purely in-memory — no DB calls during gameplay.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CombatService {

    /**
     * Skill multiplier when answer is correct: 1.5x base attack damage.
     * Rounded to nearest int.
     */
    private static final double SKILL_MULTIPLIER = 1.5;

    private final BattleRoomStore battleRoomStore;
    private final BattleLifecycleService battleLifecycleService;

    /**
     * Resolve a player's answer submission.
     * Called by BattleController on /app/battle/action.
     *
     * @param request the action payload from the client
     * @param studentId the authenticated student's ID
     */
    public synchronized void resolveAction(BattleActionRequest request, String username) {
        BattleRoom room = battleRoomStore.findById(request.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + request.getRoomId()));

        // Guard: only IN_PROGRESS rooms accept actions
        if (room.getStatus() != BattleStatus.IN_PROGRESS) {
            log.warn("Action rejected — room {} is not IN_PROGRESS", request.getRoomId());
            return;
        }

        // Guard: only the player whose turn it is can act
        if (!room.isCurrentTurnPlayerByUsername(username)) {
            log.warn("Action rejected — it's not {}'s turn in room {}", username, request.getRoomId());
            return;
        }

        // Guard: question ID must match current question
        if (room.getCurrentQuestion() == null ||
                !room.getCurrentQuestion().getQuestionId().equals(request.getQuestionId())) {
            log.warn("Action rejected — question mismatch in room {}", request.getRoomId());
            return;
        }

        PlayerSlot attacker = room.getCurrentTurnPlayer();
        PlayerSlot defender = room.getOpponentByUsername(username);
        PetState attackerPet = attacker.getPetState();
        PetState defenderPet = defender.getPetState();

        String correctOption = room.getCurrentQuestion().getCorrectOption();
        boolean isCorrect = correctOption.equalsIgnoreCase(request.getSelectedOption());

        if (isCorrect) {
            // Calculate and apply damage
            int damage = (int) Math.round(attackerPet.getAttackDamage() * SKILL_MULTIPLIER);
            defenderPet.applyDamage(damage);

            log.info("[Room {}] {} answered correctly! {} deals {} damage to {}. Defender HP: {}/{}",
                    room.getRoomId(), attacker.getUsername(), attackerPet.getPetName(),
                    damage, defenderPet.getPetName(), defenderPet.getCurrentHp(), defenderPet.getMaxHp());

            // Check win condition
            if (!defenderPet.isAlive()) {
                battleLifecycleService.endGame(room, attacker.getStudentId(), "HP_ZERO");
                return;
            }

            // Correct answer → switch turn
            battleLifecycleService.nextTurn(room, "CORRECT", damage);

        } else {
            log.info("[Room {}] {} answered WRONG. Turn forfeited.", room.getRoomId(), attacker.getUsername());
            // Wrong answer → forfeit turn, 0 damage
            battleLifecycleService.nextTurn(room, "WRONG", 0);
        }
    }
}
