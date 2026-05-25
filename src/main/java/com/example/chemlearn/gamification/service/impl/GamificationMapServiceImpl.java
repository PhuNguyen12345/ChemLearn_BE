package com.example.chemlearn.gamification.service.impl;

import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.lms.repository.StudentRepository;
import com.example.chemlearn.gamification.dto.IslandProgressDTO;
import com.example.chemlearn.gamification.dto.NodeProgressDTO;
import com.example.chemlearn.gamification.dto.ProgressMapResponse;
import com.example.chemlearn.gamification.dto.MapNodeQuestionResponse;
import com.example.chemlearn.gamification.entity.MapIsland;
import com.example.chemlearn.gamification.entity.MapNode;
import com.example.chemlearn.gamification.entity.MapNodeQuestion;
import com.example.chemlearn.gamification.entity.StudentNodeProgress;
import com.example.chemlearn.gamification.entity.XpLog;
import com.example.chemlearn.gamification.enums.XpSource;
import com.example.chemlearn.gamification.repository.MapIslandRepository;
import com.example.chemlearn.gamification.repository.MapNodeRepository;
import com.example.chemlearn.gamification.repository.MapNodeQuestionRepository;
import com.example.chemlearn.gamification.repository.StudentNodeProgressRepository;
import com.example.chemlearn.gamification.repository.XpLogRepository;
import com.example.chemlearn.gamification.service.GamificationMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GamificationMapServiceImpl implements GamificationMapService {

    private final StudentRepository studentRepository;
    private final MapIslandRepository mapIslandRepository;
    private final MapNodeRepository mapNodeRepository;
    private final StudentNodeProgressRepository studentNodeProgressRepository;
    private final MapNodeQuestionRepository mapNodeQuestionRepository;
    private final XpLogRepository xpLogRepository;

    @Override
    @Transactional(readOnly = true)
    public ProgressMapResponse getProgressMap(String username) {
        Student student = studentRepository.findByUsers_Username(username)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        int currentLevel = (student.getExperience() != null ? student.getExperience() : 0) / 1000 + 1;

        List<MapIsland> islands = mapIslandRepository.findAllByOrderByOrderIndexAsc();
        List<StudentNodeProgress> progressList = studentNodeProgressRepository.findByStudentId(student.getId());

        Map<UUID, StudentNodeProgress> progressMap = progressList.stream()
                .collect(Collectors.toMap(p -> p.getNode().getId(), Function.identity()));

        int totalStars = 0;
        List<IslandProgressDTO> islandDTOs = new ArrayList<>();

        for (MapIsland island : islands) {
            boolean isIslandLocked = island.getUnlockLevel() > currentLevel;

            List<MapNode> nodes = mapNodeRepository.findByIslandIdOrderByOrderIndexAsc(island.getId());
            List<NodeProgressDTO> nodeDTOs = new ArrayList<>();

            boolean previousNodeCompleted = true;

            for (MapNode node : nodes) {
                StudentNodeProgress progress = progressMap.get(node.getId());

                boolean isCompleted = progress != null && progress.getIsCompleted();
                int stars = progress != null ? progress.getStars() : 0;
                totalStars += stars;

                boolean isLocked = isIslandLocked || !previousNodeCompleted;

                nodeDTOs.add(NodeProgressDTO.builder()
                        .nodeId(node.getId())
                        .name(node.getName())
                        .nodeType(node.getNodeType())
                        .isCompleted(isCompleted)
                        .isLocked(isLocked)
                        .stars(stars)
                        .xpReward(node.getXpReward())
                        .targetId(node.getTargetId())
                        .monsterName(node.getMonsterName())
                        .monsterImageUrl(node.getMonsterImageUrl())
                        .monsterIdleUrl(node.getMonsterIdleUrl())
                        .monsterAttackUrl(node.getMonsterAttackUrl())
                        .monsterDamagedUrl(node.getMonsterDamagedUrl())
                        .build());

                previousNodeCompleted = isCompleted;
            }

            islandDTOs.add(IslandProgressDTO.builder()
                    .islandId(island.getId())
                    .name(island.getName())
                    .description(island.getDescription())
                    .isLocked(isIslandLocked)
                    .requiredLevel(island.getUnlockLevel())
                    .imageUrl(island.getImageUrl())
                    .nodes(nodeDTOs)
                    .build());
        }

        return ProgressMapResponse.builder()
                .islands(islandDTOs)
                .totalStars(totalStars)
                .currentLevel(currentLevel)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MapNodeQuestionResponse> getQuestionsForNode(UUID nodeId) {
        List<MapNodeQuestion> rawQuestions = mapNodeQuestionRepository.findByNodeId(nodeId);
        List<MapNodeQuestionResponse> response = new ArrayList<>();

        for (MapNodeQuestion q : rawQuestions) {
            // Determine correct answer text
            String correctAnswerText = "";
            switch (q.getCorrectOption().trim().toUpperCase()) {
                case "A":
                    correctAnswerText = q.getOptionA();
                    break;
                case "B":
                    correctAnswerText = q.getOptionB();
                    break;
                case "C":
                    correctAnswerText = q.getOptionC();
                    break;
                case "D":
                    correctAnswerText = q.getOptionD();
                    break;
                default:
                    correctAnswerText = q.getOptionA();
            }

            // Create shuffled options list
            List<String> options = new ArrayList<>(List.of(
                    q.getOptionA(),
                    q.getOptionB(),
                    q.getOptionC(),
                    q.getOptionD()
            ));
            Collections.shuffle(options);

            response.add(MapNodeQuestionResponse.builder()
                    .id(q.getId())
                    .prompt(q.getPrompt())
                    .options(options)
                    .correctAnswer(correctAnswerText)
                    .explanation(q.getExplanation())
                    .build());
        }

        // Shuffle the whole question set to make it different each game session
        Collections.shuffle(response);
        return response;
    }

    @Override
    @Transactional
    public void completeNode(String username, UUID nodeId, int stars) {
        Student student = studentRepository.findByUsers_Username(username)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        MapNode node = mapNodeRepository.findById(nodeId)
                .orElseThrow(() -> new IllegalArgumentException("Map Node not found"));

        Optional<StudentNodeProgress> existingProgressOpt =
                studentNodeProgressRepository.findByStudentIdAndNodeId(student.getId(), nodeId);

        boolean isBoss = "BOSS".equals(node.getNodeType());

        if (existingProgressOpt.isEmpty()) {
            // First time completion
            StudentNodeProgress newProgress = new StudentNodeProgress();
            newProgress.setStudent(student);
            newProgress.setNode(node);
            newProgress.setIsCompleted(true);
            newProgress.setStars(stars);
            newProgress.setCompletedAt(Instant.now());
            studentNodeProgressRepository.save(newProgress);

            // Award Full XP and Coins
            int xpReward = node.getXpReward() != null ? node.getXpReward() : 1000;
            int coinsReward = isBoss ? 500 : 300;

            student.setExperience((student.getExperience() != null ? student.getExperience() : 0) + xpReward);
            student.setCoins((student.getCoins() != null ? student.getCoins() : 0) + coinsReward);
            studentRepository.save(student);

            // Log XP Reward
            XpLog xpLog = new XpLog();
            xpLog.setStudent(student);
            xpLog.setAmount(xpReward);
            xpLog.setSource(XpSource.QUIZ);
            xpLog.setDescription("Hoàn thành lần đầu ải: " + node.getName());
            xpLogRepository.save(xpLog);
        } else {
            // Replay completion
            StudentNodeProgress existingProgress = existingProgressOpt.get();
            if (stars > existingProgress.getStars()) {
                existingProgress.setStars(stars);
                studentNodeProgressRepository.save(existingProgress);
            }

            // Award 1/10th Coins, 0 XP
            int coinsReward = isBoss ? 50 : 30;
            student.setCoins((student.getCoins() != null ? student.getCoins() : 0) + coinsReward);
            studentRepository.save(student);
        }
    }
}
