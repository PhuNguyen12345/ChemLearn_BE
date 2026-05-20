package com.example.chemlearn.lms.dto.leaderboard;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaderboardEntryDTO {
    private Integer rank;
    private String studentId;
    private String name;
    private String initials;
    private Integer exp; // We use 'exp' key to match the frontend, but it represents 'score' for the selected category
    private String trend; // 'up', 'down', 'flat'
    private Boolean isCurrentUser;
    private String avatarBg;
}
