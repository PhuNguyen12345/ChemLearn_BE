package com.example.chemlearn.lms.dto.leaderboard;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LeaderboardResponseDTO {
    private String category;
    private List<LeaderboardEntryDTO> topStudents;
    private LeaderboardEntryDTO currentUser;
    private Integer totalStudents;
}
