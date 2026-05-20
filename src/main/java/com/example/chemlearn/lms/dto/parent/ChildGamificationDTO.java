package com.example.chemlearn.lms.dto.parent;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChildGamificationDTO {
    private Integer level;
    private Integer experience;
    private Integer totalPoints;
    private List<PendingQuestDTO> pendingQuests;

    @Data
    @Builder
    public static class PendingQuestDTO {
        private String questTitle;
        private String description;
        private Integer requiredAmount;
        private Integer currentProgress;
    }
}
