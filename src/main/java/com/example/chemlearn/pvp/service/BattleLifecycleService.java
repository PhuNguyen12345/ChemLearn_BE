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
import com.example.chemlearn.pvp.model.PlayerSlot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.chemlearn.gamification.service.QuestService;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;

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

        QuestionPayload payload = QuestionPayload.builder()
                .questionId(question.getId())
                .prompt(question.getPrompt())
                .optionA(question.getOptionA())
                .optionB(question.getOptionB())
                .optionC(question.getOptionC())
                .optionD(question.getOptionD())
                .correctOption(question.getCorrectOption()) // stored, NOT sent to FE
                .build();

        room.setCurrentQuestion(payload);
        room.resetTurnDeadline();

        GameStateResponse turnState = buildGameState(room, "NEW_TURN", 0);
        broadcast(room.getRoomId(), turnState);

        log.info("[Room {}] Turn started for player {}. Question: {}",
                room.getRoomId(), room.getCurrentTurnPlayer().getUsername(), question.getId());
    }

    /**
     * Called by CombatService after resolving an action.
     * Switches turn and starts the next question.
     */
    public void nextTurn(BattleRoom room, String actionResult, int damageDealt) {
        room.switchTurn();

        GameStateResponse state = buildGameState(room, actionResult, damageDealt);
        broadcast(room.getRoomId(), state);

        java.util.concurrent.CompletableFuture.runAsync(() -> {
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
        try {
            Student winnerStudent = studentRepository.findById(UUID.fromString(winner.getStudentId()))
                    .orElseThrow(() -> new RuntimeException("Winner student not found: " + winner.getStudentId()));
            Student loserStudent = studentRepository.findById(UUID.fromString(loser.getStudentId()))
                    .orElseThrow(() -> new RuntimeException("Loser student not found: " + loser.getStudentId()));

            winnerStudent.setExperience(winnerStudent.getExperience() + WINNER_XP);
            winnerStudent.setCoins(winnerStudent.getCoins() + WINNER_COINS);
            winnerStudent.setPvpWins(winnerStudent.getPvpWins() + 1);

            loserStudent.setExperience(loserStudent.getExperience() + LOSER_XP);
            loserStudent.setCoins(loserStudent.getCoins() + LOSER_COINS);

            studentRepository.save(winnerStudent);
            studentRepository.save(loserStudent);

            // Log XP gains
            XpLog winLog = new XpLog();
            winLog.setStudent(winnerStudent);
            winLog.setAmount(WINNER_XP);
            winLog.setSource(com.example.chemlearn.gamification.enums.XpSource.PVP_WIN);
            winLog.setDescription("PVP Battle Win vs " + loser.getDisplayName());
            winLog.setCreatedAt(Instant.now());
            xpLogRepository.save(winLog);

            XpLog loseLog = new XpLog();
            loseLog.setStudent(loserStudent);
            loseLog.setAmount(LOSER_XP);
            loseLog.setSource(com.example.chemlearn.gamification.enums.XpSource.PVP_LOSS);
            loseLog.setDescription("PVP Battle Loss vs " + winner.getDisplayName());
            loseLog.setCreatedAt(Instant.now());
            xpLogRepository.save(loseLog);

            // Track PLAY_PVP daily quest progress
            try {
                questService.updateProgress(winnerStudent.getId(), "PLAY_PVP", 1);
                questService.updateProgress(loserStudent.getId(), "PLAY_PVP", 1);
            } catch (Exception e) {
                log.error("Failed to track PLAY_PVP quest progress", e);
            }
        } catch (Exception e) {
            log.error("Failed to persist battle rewards", e);
        }
    }

    private QuestionBankItem pickRandomQuestion() {
        List<QuestionBankItem> allQuestions = questionBankItemRepository.findAll();
        if (allQuestions.isEmpty()) {
            throw new IllegalStateException("No questions available in QuestionBankItem");
        }
        return allQuestions.get(random.nextInt(allQuestions.size()));
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
