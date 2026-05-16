package com.example.chemlearn.gamification.service.impl;

import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.lms.repository.StudentRepository;
import com.example.chemlearn.gamification.dto.IslandProgressDTO;
import com.example.chemlearn.gamification.dto.NodeProgressDTO;
import com.example.chemlearn.gamification.dto.ProgressMapResponse;
import com.example.chemlearn.gamification.entity.MapIsland;
import com.example.chemlearn.gamification.entity.MapNode;
import com.example.chemlearn.gamification.entity.StudentNodeProgress;
import com.example.chemlearn.gamification.repository.MapIslandRepository;
import com.example.chemlearn.gamification.repository.MapNodeRepository;
import com.example.chemlearn.gamification.repository.StudentNodeProgressRepository;
import com.example.chemlearn.gamification.service.GamificationMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GamificationMapServiceImpl implements GamificationMapService {

    private final StudentRepository studentRepository;
    private final MapIslandRepository mapIslandRepository;
    private final MapNodeRepository mapNodeRepository;
    private final StudentNodeProgressRepository studentNodeProgressRepository;

    @Override
    public ProgressMapResponse getProgressMap(String username) {
        Student student = studentRepository.findByUsers_Username(username)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        int currentLevel = (student.getExperience() != null ? student.getExperience() : 0) / 100 + 1;

        List<MapIsland> islands = mapIslandRepository.findAllByOrderByOrderIndexAsc();
        List<StudentNodeProgress> progressList = studentNodeProgressRepository.findByStudentId(student.getId());

        Map<java.util.UUID, StudentNodeProgress> progressMap = progressList.stream()
                .collect(Collectors.toMap(p -> p.getNode().getId(), Function.identity()));

        int totalStars = 0;
        List<IslandProgressDTO> islandDTOs = new ArrayList<>();

        for (MapIsland island : islands) {
            boolean isIslandLocked = island.getUnlockLevel() > currentLevel;

            List<MapNode> nodes = mapNodeRepository.findByIslandIdOrderByOrderIndexAsc(island.getId());
            List<NodeProgressDTO> nodeDTOs = new ArrayList<>();

            boolean previousNodeCompleted = true; // First node is unlocked by default if island is unlocked

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
                        .build());

                // For the next node in the loop
                previousNodeCompleted = isCompleted;
            }

            islandDTOs.add(IslandProgressDTO.builder()
                    .islandId(island.getId())
                    .name(island.getName())
                    .description(island.getDescription())
                    .isLocked(isIslandLocked)
                    .requiredLevel(island.getUnlockLevel())
                    .nodes(nodeDTOs)
                    .build());
        }

        return ProgressMapResponse.builder()
                .islands(islandDTOs)
                .totalStars(totalStars)
                .currentLevel(currentLevel)
                .build();
    }
}
