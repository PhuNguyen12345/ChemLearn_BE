package com.example.chemlearn.lms.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminDashboardSummaryDTO {
    private long totalUsers;
    private long activeUsers;
    private long students;
    private long teachers;
    private long parents;
    private long chapters;
    private long lessons;
    private long labs;
    private long labProgressRecords;
    private long mapIslands;
    private long mapNodes;
    private long nodeProgressRecords;
    private long petSpecies;
    private long studentPets;
    private long eggItems;
    private long openReports;
    private long totalReports;
    private long completedLessonProgress;
    private long inProgressLessonProgress;
    private List<ModuleUsageDTO> moduleUsage;
    private List<RoleBreakdownDTO> roleBreakdown;

    @Getter
    @Builder
    public static class ModuleUsageDTO {
        private String name;
        private long total;
        private long activity;
        private int health;
    }

    @Getter
    @Builder
    public static class RoleBreakdownDTO {
        private String role;
        private long count;
    }
}
