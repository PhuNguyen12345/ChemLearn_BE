package com.example.chemlearn.pvp.service;

import com.example.chemlearn.gamification.entity.StudentPet;
import com.example.chemlearn.gamification.repository.StudentPetRepository;
import com.example.chemlearn.lms.repository.StudentRepository;
import com.example.chemlearn.pvp.dto.GameStateResponse;
import com.example.chemlearn.pvp.enums.BattleStatus;
import com.example.chemlearn.pvp.model.BattleRoom;
import com.example.chemlearn.pvp.model.PetState;
import com.example.chemlearn.pvp.model.PlayerSlot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Manages the matchmaking queue and pairs two players into a BattleRoom.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchmakingService {

    private final StudentPetRepository studentPetRepository;
    private final StudentRepository studentRepository;
    private final BattleRoomStore battleRoomStore;
    private final BattleLifecycleService battleLifecycleService;
    private final SimpMessagingTemplate messagingTemplate;

    /** Thread-safe FIFO queue for waiting players */
    private final ConcurrentLinkedQueue<MatchmakingEntry> queue = new ConcurrentLinkedQueue<>();

    /**
     * Called when a student sends /app/battle/join.
     * Adds them to queue, then attempts to match.
     */
    @org.springframework.transaction.annotation.Transactional
    public synchronized void joinQueue(String username, UUID studentPetId, String displayName,
                                       String wsSessionId) {
        // Resolve student by username
        var student = studentRepository.findByUsers_Username(username)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + username));
        String studentId = student.getId().toString();

        // Remove any stale entry for this student (re-queue guard)
        queue.removeIf(e -> e.studentId().equals(studentId));

        // Check if already in a battle
        if (battleRoomStore.findByStudentId(studentId).isPresent()) {
            log.warn("Student {} tried to join queue but is already in a battle", username);
            return;
        }

        // Load pet from DB and validate ownership
        StudentPet pet = studentPetRepository.findById(studentPetId)
                .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + studentPetId));

        if (!pet.getStudent().getId().toString().equals(studentId)) {
            throw new SecurityException("Student does not own this pet");
        }

        PetState petState = buildPetState(pet);
        MatchmakingEntry entry = new MatchmakingEntry(studentId, username, displayName, wsSessionId, petState);
        queue.offer(entry);

        log.info("Student {} joined matchmaking queue. Queue size: {}", username, queue.size());

        // Try to match
        tryMatch();
    }

    /**
     * Called when a student wants to explicitly leave the queue
     */
    public synchronized void leaveQueue(String username) {
        queue.removeIf(e -> e.username().equals(username));
        log.info("Student {} left matchmaking queue. Queue size: {}", username, queue.size());
    }

    private synchronized void tryMatch() {
        if (queue.size() < 2) return;

        MatchmakingEntry entry1 = queue.poll();
        MatchmakingEntry entry2 = queue.poll();

        if (entry1 == null || entry2 == null) return;

        // Create room
        String roomId = UUID.randomUUID().toString();

        PlayerSlot player1 = PlayerSlot.builder()
                .studentId(entry1.studentId())
                .username(entry1.username())
                .displayName(entry1.displayName())
                .wsSessionId(entry1.wsSessionId())
                .petState(entry1.petState())
                .build();

        PlayerSlot player2 = PlayerSlot.builder()
                .studentId(entry2.studentId())
                .username(entry2.username())
                .displayName(entry2.displayName())
                .wsSessionId(entry2.wsSessionId())
                .petState(entry2.petState())
                .build();

        BattleRoom room = BattleRoom.builder()
                .roomId(roomId)
                .player1(player1)
                .player2(player2)
                .status(BattleStatus.IN_PROGRESS)
                .build();

        battleRoomStore.save(room);

        log.info("Matched {} vs {} in room {}", entry1.username(), entry2.username(), roomId);

        // Start the battle lifecycle
        battleLifecycleService.startBattle(room);
    }

    /**
     * Build PetState from DB entity with stat calculation.
     * Formula: effective = base + level * growth
     */
    private PetState buildPetState(StudentPet pet) {
        var species = pet.getSpecies();
        int level = pet.getLevel();

        int maxHp = species.getBaseHp() + (level * species.getHpGrowth());
        int attack = species.getBaseDamage() + (level * species.getDamageGrowth());

        return PetState.builder()
                .studentPetId(pet.getId())
                .petName(species.getName())
                .element(species.getElement())
                .imageUrl(species.getImageUrl())
                .skillName(species.getSkillName())
                .maxHp(maxHp)
                .currentHp(maxHp)
                .attackDamage(attack)
                .petLevel(level)
                .build();
    }

    /** Internal matchmaking entry record */
    private record MatchmakingEntry(
            String studentId,
            String username,
            String displayName,
            String wsSessionId,
            PetState petState
    ) {}
}
