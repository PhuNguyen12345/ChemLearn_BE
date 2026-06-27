package com.example.chemlearn.pvp.service;

import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.gamification.entity.XpLog;
import com.example.chemlearn.gamification.repository.XpLogRepository;
import com.example.chemlearn.lms.repository.StudentRepository;
import com.example.chemlearn.lms.repository.QuestionBankItemRepository;
import com.example.chemlearn.lms.entity.QuestionBankItem;
import com.example.chemlearn.pvp.dto.BattleResultResponse;
import com.example.chemlearn.pvp.dto.GameStateResponse;
import com.example.chemlearn.pvp.dto.QuestionPayload;
import com.example.chemlearn.pvp.enums.BattleStatus;
import com.example.chemlearn.pvp.model.BattleRoom;
import com.example.chemlearn.pvp.model.PetState;
import com.example.chemlearn.pvp.model.PlayerSlot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.chemlearn.gamification.service.QuestService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages the lifecycle of a battle: start, turn transitions, timeouts, and end game.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BattleLifecycleService {

    private static final int WINNER_XP = 100;
    private static final int WINNER_COINS = 50;
    private static final int LOSER_XP = 20;
    private static final int LOSER_COINS = 10;
    private static final double SKILL_MULTIPLIER = 1.5;
    private static final double AUTOMATED_CORRECT_RATE = 0.62;
    private static final int AUTOMATED_MIN_DELAY_MS = 1800;
    private static final int AUTOMATED_MAX_DELAY_MS = 6200;

    private final SimpMessagingTemplate messagingTemplate;
    private final QuestionBankItemRepository questionBankItemRepository;
    private final StudentRepository studentRepository;
    private final XpLogRepository xpLogRepository;
    private final BattleRoomStore battleRoomStore;
    private final QuestService questService;

    private final Random random = new Random();

    /**
     * Called by MatchmakingService once both players are matched.
     * Broadcasts BATTLE_START state and then kicks off the first turn.
     */
    public void startBattle(BattleRoom room) {
        room.setStatus(BattleStatus.IN_PROGRESS);
        room.resetTurnDeadline();

        GameStateResponse startState = buildGameState(room, "BATTLE_START", 0);
        broadcast(room.getRoomId(), startState);
        
        // Also broadcast to a global match topic so clients can discover their roomId
        messagingTemplate.convertAndSend("/topic/battle/match", startState);

        log.info("[Room {}] Battle started! {} vs {}", room.getRoomId(),
                room.getPlayer1().getUsername(), room.getPlayer2().getUsername());

        startTurn(room);
    }

    /**
     * Begin a new turn: pick a random question and broadcast to both players.
     */
    public void startTurn(BattleRoom room) {
        QuestionBankItem question = pickRandomQuestion();

        QuestionPayload payload = buildShuffledQuestionPayload(question);

        room.setCurrentQuestion(payload);
        room.resetTurnDeadline();

        GameStateResponse turnState = buildGameState(room, "NEW_TURN", 0);
        broadcast(room.getRoomId(), turnState);

        log.info("[Room {}] Turn started for player {}. Question: {}",
                room.getRoomId(), room.getCurrentTurnPlayer().getUsername(), question.getId());

        scheduleAutomatedActionIfNeeded(room, payload);
    }

    /**
     * Called by CombatService after resolving an action.
     * Switches turn and starts the next question.
     */
    public void nextTurn(BattleRoom room, String actionResult, int damageDealt) {
        room.switchTurn();

        GameStateResponse state = buildGameState(room, actionResult, damageDealt);
        broadcast(room.getRoomId(), state);

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (room.getStatus() == BattleStatus.IN_PROGRESS) {
                startTurn(room);
            }
        });
    }

    /**
     * End the battle. Persist results to DB and broadcast final state.
     */
    @Transactional
    public void endGame(BattleRoom room, String winnerId, String endReason) {
        if (room.getStatus() != BattleStatus.IN_PROGRESS) return; // Guard against double-end

        PlayerSlot winner = room.getPlayer(winnerId);
        PlayerSlot loser = winner.equals(room.getPlayer1()) ? room.getPlayer2() : room.getPlayer1();

        // Determine BattleStatus from winner's slot
        BattleStatus finalStatus = winner.equals(room.getPlayer1())
                ? BattleStatus.PLAYER1_WON : BattleStatus.PLAYER2_WON;
        room.setStatus(finalStatus);

        // --- Persist results to DB (Eventual Consistency) ---
        persistRewards(winner, loser);

        // Build and broadcast final result
        BattleResultResponse result = BattleResultResponse.builder()
                .roomId(room.getRoomId())
                .winnerId(winner.getStudentId())
                .winnerName(winner.getDisplayName())
                .loserId(loser.getStudentId())
                .loserName(loser.getDisplayName())
                .winnerXpGained(WINNER_XP)
                .winnerCoinsGained(WINNER_COINS)
                .loserXpGained(LOSER_XP)
                .loserCoinsGained(LOSER_COINS)
                .endReason(endReason)
                .build();

        messagingTemplate.convertAndSend("/topic/battle/" + room.getRoomId() + "/result", result);

        // Clean up room from memory after a short delay (let FE receive result)
        // In production, use a ScheduledExecutor to delay removal
        battleRoomStore.remove(room.getRoomId());

        log.info("[Room {}] Battle ended. Winner: {}. Reason: {}",
                room.getRoomId(), winner.getUsername(), endReason);
    }

    /**
     * @Scheduled task: checks every 5 seconds for expired turns.
     * If a turn has timed out, auto-switch turn (treat as wrong answer).
     */
    @Scheduled(fixedDelay = 5000)
    public void checkTimeouts() {
        for (BattleRoom room : battleRoomStore.findAll()) {
            if (room.getStatus() == BattleStatus.IN_PROGRESS && room.isTurnExpired()) {
                log.info("[Room {}] Turn timeout for player {}",
                        room.getRoomId(), room.getCurrentTurnPlayer().getUsername());

                // Treat timeout as wrong answer: switch turn without dealing damage
                nextTurn(room, "TIMEOUT", 0);
            }
        }
    }

    // --- Private helpers ---

    private void persistRewards(PlayerSlot winner, PlayerSlot loser) {
        applyBattleReward(winner, loser, true);
        applyBattleReward(loser, winner, false);
    }

    private void applyBattleReward(PlayerSlot player, PlayerSlot opponent, boolean won) {
        if (player.isAutomated()) {
            return;
        }

        try {
            Student student = studentRepository.findById(UUID.fromString(player.getStudentId()))
                    .orElseThrow(() -> new RuntimeException("Student not found: " + player.getStudentId()));

            int xpGained = won ? WINNER_XP : LOSER_XP;
            int coinsGained = won ? WINNER_COINS : LOSER_COINS;

            student.setExperience(valueOrZero(student.getExperience()) + xpGained);
            student.setCoins(valueOrZero(student.getCoins()) + coinsGained);
            if (won) {
                student.setPvpWins(valueOrZero(student.getPvpWins()) + 1);
            }

            studentRepository.save(student);

            XpLog xpLog = new XpLog();
            xpLog.setStudent(student);
            xpLog.setAmount(xpGained);
            xpLog.setSource(won
                    ? com.example.chemlearn.gamification.enums.XpSource.PVP_WIN
                    : com.example.chemlearn.gamification.enums.XpSource.PVP_LOSS);
            xpLog.setDescription("PVP Battle " + (won ? "Win" : "Loss") + " vs " + opponent.getDisplayName());
            xpLog.setCreatedAt(Instant.now());
            xpLogRepository.save(xpLog);

            try {
                questService.updateProgress(student.getId(), "PLAY_PVP", 1);
            } catch (Exception e) {
                log.error("Failed to track PLAY_PVP quest progress", e);
            }
        } catch (Exception e) {
            log.error("Failed to persist battle reward for {}", player.getUsername(), e);
        }
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private void scheduleAutomatedActionIfNeeded(BattleRoom room, QuestionPayload payload) {
        PlayerSlot current = room.getCurrentTurnPlayer();
        if (!current.isAutomated()) {
            return;
        }

        int delayMs = ThreadLocalRandom.current().nextInt(AUTOMATED_MIN_DELAY_MS, AUTOMATED_MAX_DELAY_MS + 1);
        UUID questionId = payload.getQuestionId();
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            resolveAutomatedAction(room.getRoomId(), questionId);
        });
    }

    private synchronized void resolveAutomatedAction(String roomId, UUID questionId) {
        BattleRoom room = battleRoomStore.findById(roomId).orElse(null);
        if (room == null || room.getStatus() != BattleStatus.IN_PROGRESS) {
            return;
        }
        if (room.getCurrentQuestion() == null || !room.getCurrentQuestion().getQuestionId().equals(questionId)) {
            return;
        }

        PlayerSlot attacker = room.getCurrentTurnPlayer();
        if (!attacker.isAutomated()) {
            return;
        }

        PlayerSlot defender = room.getOpponent(attacker.getStudentId());
        PetState attackerPet = attacker.getPetState();
        PetState defenderPet = defender.getPetState();

        String selectedOption = chooseAutomatedAnswer(room.getCurrentQuestion());
        boolean isCorrect = room.getCurrentQuestion().getCorrectOption().equalsIgnoreCase(selectedOption);

        if (isCorrect) {
            int damage = (int) Math.round(attackerPet.getAttackDamage() * SKILL_MULTIPLIER);
            defenderPet.applyDamage(damage);

            log.info("[Room {}] {} answered correctly. {} deals {} damage to {}. Defender HP: {}/{}",
                    room.getRoomId(), attacker.getUsername(), attackerPet.getPetName(),
                    damage, defenderPet.getPetName(), defenderPet.getCurrentHp(), defenderPet.getMaxHp());

            if (!defenderPet.isAlive()) {
                endGame(room, attacker.getStudentId(), "HP_ZERO");
                return;
            }

            nextTurn(room, "CORRECT", damage);
        } else {
            log.info("[Room {}] {} answered wrong. Turn forfeited.", room.getRoomId(), attacker.getUsername());
            nextTurn(room, "WRONG", 0);
        }
    }

    private String chooseAutomatedAnswer(QuestionPayload question) {
        String correctOption = normalizeOptionKey(question.getCorrectOption());
        if (ThreadLocalRandom.current().nextDouble() < AUTOMATED_CORRECT_RATE) {
            return correctOption;
        }

        List<String> optionKeys = List.of("A", "B", "C", "D");
        List<String> wrongOptions = optionKeys.stream()
                .filter(option -> !option.equalsIgnoreCase(correctOption))
                .toList();
        return wrongOptions.get(ThreadLocalRandom.current().nextInt(wrongOptions.size()));
    }

    private QuestionBankItem pickRandomQuestion() {
        List<QuestionBankItem> allQuestions = questionBankItemRepository.findAll();
        if (allQuestions.isEmpty()) {
            throw new IllegalStateException("No questions available in QuestionBankItem");
        }
        return allQuestions.get(random.nextInt(allQuestions.size()));
    }

    private QuestionPayload buildShuffledQuestionPayload(QuestionBankItem question) {
        List<AnswerOption> options = new ArrayList<>();
        options.add(new AnswerOption("A", question.getOptionA()));
        options.add(new AnswerOption("B", question.getOptionB()));
        options.add(new AnswerOption("C", question.getOptionC()));
        options.add(new AnswerOption("D", question.getOptionD()));

        Collections.shuffle(options, random);

        String correctOption = normalizeOptionKey(question.getCorrectOption());
        String shuffledCorrectOption = "A";
        String[] displayKeys = {"A", "B", "C", "D"};
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).key().equals(correctOption)) {
                shuffledCorrectOption = displayKeys[i];
                break;
            }
        }

        return QuestionPayload.builder()
                .questionId(question.getId())
                .prompt(question.getPrompt())
                .optionA(options.get(0).text())
                .optionB(options.get(1).text())
                .optionC(options.get(2).text())
                .optionD(options.get(3).text())
                .correctOption(shuffledCorrectOption)
                .build();
    }

    private String normalizeOptionKey(String option) {
        if (option == null || option.isBlank()) {
            return "A";
        }
        return option.trim().substring(0, 1).toUpperCase();
    }

    private record AnswerOption(String key, String text) {
    }

    private void broadcast(String roomId, GameStateResponse state) {
        messagingTemplate.convertAndSend("/topic/battle/" + roomId, state);
    }

    public GameStateResponse buildGameState(BattleRoom room, String lastAction, int damageDealt) {
        PlayerSlot current = room.getCurrentTurnPlayer();

        // Strip correct answer before building response (security)
        QuestionPayload safeQuestion = null;
        if (room.getCurrentQuestion() != null) {
            QuestionPayload q = room.getCurrentQuestion();
            safeQuestion = QuestionPayload.builder()
                    .questionId(q.getQuestionId())
                    .prompt(q.getPrompt())
                    .optionA(q.getOptionA())
                    .optionB(q.getOptionB())
                    .optionC(q.getOptionC())
                    .optionD(q.getOptionD())
                    // correctOption intentionally omitted
                    .build();
        }

        return GameStateResponse.builder()
                .roomId(room.getRoomId())
                .status(room.getStatus())
                .player1Id(room.getPlayer1().getStudentId())
                .player1Name(room.getPlayer1().getDisplayName())
                .player1Pet(room.getPlayer1().getPetState())
                .player2Id(room.getPlayer2().getStudentId())
                .player2Name(room.getPlayer2().getDisplayName())
                .player2Pet(room.getPlayer2().getPetState())
                .currentTurnIndex(room.getCurrentTurnIndex().get())
                .currentTurnPlayerId(current.getStudentId())
                .currentQuestion(safeQuestion)
                .turnDeadline(room.getTurnDeadline())
                .lastActionResult(lastAction)
                .lastDamageDealt(damageDealt)
                .build();
    }
}
