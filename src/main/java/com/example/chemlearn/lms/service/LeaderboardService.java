package com.example.chemlearn.lms.service;

import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.lms.dto.leaderboard.LeaderboardEntryDTO;
import com.example.chemlearn.lms.dto.leaderboard.LeaderboardResponseDTO;
import com.example.chemlearn.lms.entity.RankingHistory;
import com.example.chemlearn.lms.enums.LeaderboardCategory;
import com.example.chemlearn.lms.repository.RankingHistoryRepository;
import com.example.chemlearn.lms.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final StudentRepository studentRepository;
    private final RankingHistoryRepository rankingHistoryRepository;

    private static final String[] AVATAR_COLORS = {
            "bg-violet-500", "bg-pink-500", "bg-teal-500", "bg-blue-500",
            "bg-indigo-500", "bg-emerald-500", "bg-orange-500", "bg-rose-500",
            "bg-amber-500", "bg-cyan-500"
    };

    public LeaderboardResponseDTO getLeaderboard(String username, LeaderboardCategory category, int limit) {
        Student currentUser = studentRepository.findByUsers_Username(username)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        PageRequest pageRequest = PageRequest.of(0, limit);
        Page<Student> topStudentsPage;

        switch (category) {
            case EXPERIENCE:
                topStudentsPage = studentRepository.findAllByOrderByExperienceDesc(pageRequest);
                break;
            case STREAK:
                topStudentsPage = studentRepository.findAllByOrderByCurrentStreakDesc(pageRequest);
                break;
            case PVP_WINS:
                topStudentsPage = studentRepository.findAllByOrderByPvpWinsDesc(pageRequest);
                break;
            default:
                throw new IllegalArgumentException("Unknown category");
        }

        List<LeaderboardEntryDTO> topEntries = new ArrayList<>();
        int rank = 1;
        for (Student s : topStudentsPage.getContent()) {
            topEntries.add(buildEntry(s, rank, category, currentUser.getId()));
            rank++;
        }

        // Calculate current user's rank
        int currentUserRank = calculateUserRank(currentUser, category);
        LeaderboardEntryDTO currentUserEntry = buildEntry(currentUser, currentUserRank, category, currentUser.getId());

        int totalStudents = (int) studentRepository.count();

        return LeaderboardResponseDTO.builder()
                .category(category.name())
                .topStudents(topEntries)
                .currentUser(currentUserEntry)
                .totalStudents(totalStudents)
                .build();
    }

    private int calculateUserRank(Student student, LeaderboardCategory category) {
        int higherCount = 0;
        switch (category) {
            case EXPERIENCE:
                higherCount = studentRepository.countByExperienceGreaterThan(student.getExperience() != null ? student.getExperience() : 0);
                break;
            case STREAK:
                higherCount = studentRepository.countByCurrentStreakGreaterThan(student.getCurrentStreak() != null ? student.getCurrentStreak() : 0);
                break;
            case PVP_WINS:
                higherCount = studentRepository.countByPvpWinsGreaterThan(student.getPvpWins() != null ? student.getPvpWins() : 0);
                break;
        }
        return higherCount + 1;
    }

    private LeaderboardEntryDTO buildEntry(Student student, int rank, LeaderboardCategory category, UUID currentUserId) {
        int score = 0;
        switch (category) {
            case EXPERIENCE: score = student.getExperience() != null ? student.getExperience() : 0; break;
            case STREAK: score = student.getCurrentStreak() != null ? student.getCurrentStreak() : 0; break;
            case PVP_WINS: score = student.getPvpWins() != null ? student.getPvpWins() : 0; break;
        }

        String fullName = student.getUsers() != null ? student.getUsers().getFullName() : "Unknown";
        String initials = getInitials(fullName);
        String avatarBg = AVATAR_COLORS[Math.abs(student.getId().hashCode()) % AVATAR_COLORS.length];
        boolean isCurrentUser = student.getId().equals(currentUserId);

        // Calculate trend comparing to yesterday's snapshot
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String trend = "flat";
        Optional<RankingHistory> history = rankingHistoryRepository.findByStudentIdAndCategoryAndRecordDate(student.getId(), category, yesterday);
        if (history.isPresent()) {
            int previousRank = history.get().getRankValue();
            if (previousRank > rank) {
                trend = "up";
            } else if (previousRank < rank) {
                trend = "down";
            }
        }

        return LeaderboardEntryDTO.builder()
                .rank(rank)
                .studentId(student.getId().toString())
                .name(fullName)
                .initials(initials)
                .exp(score)
                .trend(trend)
                .isCurrentUser(isCurrentUser)
                .avatarBg(avatarBg)
                .build();
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "UN";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
