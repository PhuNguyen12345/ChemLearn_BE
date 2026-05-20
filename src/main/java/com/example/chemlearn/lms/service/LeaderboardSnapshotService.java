package com.example.chemlearn.lms.service;

import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.lms.entity.RankingHistory;
import com.example.chemlearn.lms.enums.LeaderboardCategory;
import com.example.chemlearn.lms.repository.RankingHistoryRepository;
import com.example.chemlearn.lms.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardSnapshotService {

    private final StudentRepository studentRepository;
    private final RankingHistoryRepository rankingHistoryRepository;

    /**
     * Runs every day at 00:00 to save the ranking snapshot for all students.
     * For production with thousands of users, this should use batch processing.
     * For this MVP, we fetch pages and save.
     */
    @Scheduled(cron = "0 0 0 * * *") // Midnight every day
    @Transactional
    public void snapshotDailyRankings() {
        log.info("Starting daily ranking snapshot...");
        LocalDate today = LocalDate.now();

        // Snapshot Experience
        snapshotCategory(LeaderboardCategory.EXPERIENCE, today);

        // Snapshot Streak
        snapshotCategory(LeaderboardCategory.STREAK, today);

        // Snapshot PVP Wins
        snapshotCategory(LeaderboardCategory.PVP_WINS, today);

        log.info("Completed daily ranking snapshot.");
    }

    private void snapshotCategory(LeaderboardCategory category, LocalDate date) {
        int pageNumber = 0;
        int pageSize = 100;
        int currentRank = 1;

        while (true) {
            PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
            Page<Student> page;

            switch (category) {
                case EXPERIENCE:
                    page = studentRepository.findAllByOrderByExperienceDesc(pageRequest);
                    break;
                case STREAK:
                    page = studentRepository.findAllByOrderByCurrentStreakDesc(pageRequest);
                    break;
                case PVP_WINS:
                    page = studentRepository.findAllByOrderByPvpWinsDesc(pageRequest);
                    break;
                default:
                    return;
            }

            List<Student> students = page.getContent();
            if (students.isEmpty()) {
                break;
            }

            for (Student student : students) {
                // Determine score
                int score = 0;
                switch (category) {
                    case EXPERIENCE: score = student.getExperience() != null ? student.getExperience() : 0; break;
                    case STREAK: score = student.getCurrentStreak() != null ? student.getCurrentStreak() : 0; break;
                    case PVP_WINS: score = student.getPvpWins() != null ? student.getPvpWins() : 0; break;
                }

                // Check if already exists (idempotency)
                if (rankingHistoryRepository.findByStudentIdAndCategoryAndRecordDate(student.getId(), category, date).isEmpty()) {
                    RankingHistory history = new RankingHistory();
                    history.setStudent(student);
                    history.setCategory(category);
                    history.setRecordDate(date);
                    history.setRankValue(currentRank);
                    history.setScore(score);
                    rankingHistoryRepository.save(history);
                }
                currentRank++;
            }

            if (!page.hasNext()) {
                break;
            }
            pageNumber++;
        }
    }
}
