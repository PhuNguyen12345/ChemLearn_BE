package com.example.chemlearn.pvp.service;

import com.example.chemlearn.gamification.entity.StudentPet;
import com.example.chemlearn.gamification.repository.StudentPetRepository;
import com.example.chemlearn.lms.repository.StudentRepository;
import com.example.chemlearn.pvp.enums.BattleStatus;
import com.example.chemlearn.pvp.model.BattleRoom;
import com.example.chemlearn.pvp.model.PetState;
import com.example.chemlearn.pvp.model.PlayerSlot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages the matchmaking queue and pairs two players into a BattleRoom.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchmakingService {

    private static final long FALLBACK_MATCH_DELAY_MS = 10_000L;

    private static final String[] FALLBACK_DISPLAY_NAMES = {
            "Minh Khoa", "Bao Ngoc", "Gia Huy", "Khanh Linh", "Tuan Minh",
            "Mai Anh", "Hoang Nam", "An Nhien", "Quynh Anh", "Duc Anh"
    };

    private static final SyntheticPet[] FALLBACK_PETS = {
            new SyntheticPet("Capybara Wizard", "EARTH", "Binh tam", 820, 92),
            new SyntheticPet("Doge Wizard", "LIGHT", "Anh sang", 760, 98),
            new SyntheticPet("Skibidi Tolem", "WATER", "Song ap suat", 900, 84),
            new SyntheticPet("Tung Sahur Warrior", "EARTH", "Dia chan", 940, 88)
    };

    private final StudentPetRepository studentPetRepository;
    private final StudentRepository studentRepository;
    private final BattleRoomStore battleRoomStore;
    private final BattleLifecycleService battleLifecycleService;

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
        MatchmakingEntry entry = new MatchmakingEntry(
                studentId,
                username,
                displayName,
                wsSessionId,
                petState,
                System.currentTimeMillis(),
                false
        );
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

    @Scheduled(fixedDelay = 1000)
    public synchronized void matchExpiredQueueEntriesWithFallbackPlayers() {
        tryMatch();

        long now = System.currentTimeMillis();
        while (true) {
            MatchmakingEntry waitingEntry = queue.peek();
            if (waitingEntry == null) return;
            if (now - waitingEntry.joinedAtMillis() < FALLBACK_MATCH_DELAY_MS) return;
            if (!queue.remove(waitingEntry)) continue;

            MatchmakingEntry fallbackOpponent = buildFallbackOpponent(waitingEntry);
            createRoom(waitingEntry, fallbackOpponent);
        }
    }

    private synchronized void tryMatch() {
        while (queue.size() >= 2) {
            MatchmakingEntry entry1 = queue.poll();
            MatchmakingEntry entry2 = queue.poll();

            if (entry1 == null || entry2 == null) return;

            createRoom(entry1, entry2);
        }
    }

    private void createRoom(MatchmakingEntry entry1, MatchmakingEntry entry2) {
        // Create room
        String roomId = UUID.randomUUID().toString();

        PlayerSlot player1 = toPlayerSlot(entry1);
        PlayerSlot player2 = toPlayerSlot(entry2);

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

    private PlayerSlot toPlayerSlot(MatchmakingEntry entry) {
        return PlayerSlot.builder()
                .studentId(entry.studentId())
                .username(entry.username())
                .displayName(entry.displayName())
                .wsSessionId(entry.wsSessionId())
                .petState(entry.petState())
                .automated(entry.automated())
                .build();
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

    private MatchmakingEntry buildFallbackOpponent(MatchmakingEntry waitingEntry) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String syntheticId = UUID.randomUUID().toString();
        String displayName = FALLBACK_DISPLAY_NAMES[random.nextInt(FALLBACK_DISPLAY_NAMES.length)];

        return new MatchmakingEntry(
                syntheticId,
                "student_" + syntheticId.substring(0, 8),
                displayName,
                null,
                buildFallbackPetState(waitingEntry.petState()),
                System.currentTimeMillis(),
                true
        );
    }

    private PetState buildFallbackPetState(PetState waitingPet) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        SyntheticPet pet = FALLBACK_PETS[random.nextInt(FALLBACK_PETS.length)];

        int baseLevel = waitingPet != null ? waitingPet.getPetLevel() : 1;
        int level = Math.max(1, baseLevel + random.nextInt(-1, 2));
        int maxHp = waitingPet != null
                ? Math.max(80, waitingPet.getMaxHp() + random.nextInt(-40, 41))
                : pet.maxHp();
        int attack = waitingPet != null
                ? Math.max(10, waitingPet.getAttackDamage() + random.nextInt(-6, 7))
                : pet.attackDamage();

        return PetState.builder()
                .studentPetId(UUID.randomUUID())
                .petName(pet.name())
                .element(pet.element())
                .imageUrl(null)
                .skillName(pet.skillName())
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
            PetState petState,
            long joinedAtMillis,
            boolean automated
    ) {}

    private record SyntheticPet(
            String name,
            String element,
            String skillName,
            int maxHp,
            int attackDamage
    ) {}
}
