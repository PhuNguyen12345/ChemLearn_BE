package com.example.chemlearn.gamification.service.impl;

import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.gamification.dto.response.QuestResponse;
import com.example.chemlearn.gamification.entity.Quest;
import com.example.chemlearn.gamification.entity.StudentQuest;
import com.example.chemlearn.gamification.enums.QuestCategory;
import com.example.chemlearn.gamification.enums.XpSource;
import com.example.chemlearn.gamification.repository.QuestRepository;
import com.example.chemlearn.gamification.repository.StudentQuestRepository;
import com.example.chemlearn.gamification.service.GamificationProfileService;
import com.example.chemlearn.gamification.service.QuestService;
import com.example.chemlearn.lms.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestServiceImpl implements QuestService {

    private final StudentQuestRepository studentQuestRepository;
    private final QuestRepository questRepository;
    private final StudentRepository studentRepository;
    private final GamificationProfileService gamificationProfileService;

    @Override
    @Transactional
    public List<QuestResponse> getDailyQuests(UUID studentId) {
        LocalDate today = LocalDate.now();
        List<StudentQuest> todayQuests = studentQuestRepository.findByStudentIdAndAssignedDate(studentId, today);

        if (todayQuests.isEmpty()) {
            todayQuests = assignDailyQuests(studentId, today);
        }

        return todayQuests.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateProgress(UUID studentId, String actionType, int amount) {
        LocalDate today = LocalDate.now();
        List<StudentQuest> todayQuests = studentQuestRepository.findByStudentIdAndAssignedDate(studentId, today);
        
        for (StudentQuest sq : todayQuests) {
            if (!sq.getIsClaimed() && sq.getQuest().getActionType().equalsIgnoreCase(actionType)) {
                int current = sq.getCurrentProgress() != null ? sq.getCurrentProgress() : 0;
                int target = sq.getQuest().getTargetValue();
                
                if (current < target) {
                    sq.setCurrentProgress(Math.min(current + amount, target));
                    studentQuestRepository.save(sq);
                }
            }
        }
    }

    @Override
    @Transactional
    public QuestResponse claimQuest(UUID studentId, UUID questId) {
        LocalDate today = LocalDate.now();
        StudentQuest sq = studentQuestRepository.findByStudentIdAndQuestIdAndAssignedDate(studentId, questId, today)
                .orElseThrow(() -> new RuntimeException("Quest not found or not assigned today"));

        if (sq.getIsClaimed()) {
            throw new RuntimeException("Quest already claimed");
        }

        int current = sq.getCurrentProgress() != null ? sq.getCurrentProgress() : 0;
        int target = sq.getQuest().getTargetValue();

        if (current < target) {
            throw new RuntimeException("Quest not completed yet");
        }

        // Claim it
        sq.setIsClaimed(true);
        studentQuestRepository.save(sq);

        // Give rewards
        Quest quest = sq.getQuest();
        gamificationProfileService.addExpAndCoins(
                studentId, 
                quest.getRewardXp() != null ? quest.getRewardXp() : 0, 
                quest.getRewardCoins() != null ? quest.getRewardCoins() : 0, 
                XpSource.QUEST, 
                "Completed quest: " + quest.getTitle()
        );

        return mapToResponse(sq);
    }

    private List<StudentQuest> assignDailyQuests(UUID studentId, LocalDate date) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<Quest> activeDailyQuests = questRepository.findByCategoryAndIsActiveTrue(QuestCategory.DAILY_QUEST);
        
        // Ensure the 5 default quests exist if the DB is empty
        if (activeDailyQuests.isEmpty()) {
            activeDailyQuests = createDefaultDailyQuests();
        }

        List<StudentQuest> assigned = new ArrayList<>();
        for (Quest quest : activeDailyQuests) {
            StudentQuest sq = new StudentQuest();
            sq.setStudent(student);
            sq.setQuest(quest);
            sq.setAssignedDate(date);
            sq.setCurrentProgress(0);
            sq.setIsClaimed(false);
            assigned.add(studentQuestRepository.save(sq));
        }

        return assigned;
    }

    private List<Quest> createDefaultDailyQuests() {
        List<Quest> defaults = new ArrayList<>();
        
        defaults.add(createQuest("Hoàn thành bài Thực hành", "DO_LAB", 1, 100, 50));
        defaults.add(createQuest("Học một bài học mới", "LEARN_LESSON", 1, 100, 50));
        defaults.add(createQuest("Đăng nhập vào hệ thống", "LOGIN", 1, 50, 20));
        defaults.add(createQuest("Cho Pet ăn", "FEED_PET", 1, 80, 40));
        defaults.add(createQuest("Tham gia Đấu trường (PVP)", "PLAY_PVP", 1, 150, 100));
        
        return defaults;
    }

    private Quest createQuest(String title, String actionType, int target, int xp, int coins) {
        Quest q = new Quest();
        q.setTitle(title);
        q.setActionType(actionType);
        q.setTargetValue(target);
        q.setRewardXp(xp);
        q.setRewardCoins(coins);
        q.setCategory(QuestCategory.DAILY_QUEST);
        q.setIsActive(true);
        return questRepository.save(q);
    }

    private QuestResponse mapToResponse(StudentQuest sq) {
        return QuestResponse.builder()
                .id(sq.getQuest().getId())
                .title(sq.getQuest().getTitle())
                .actionType(sq.getQuest().getActionType())
                .targetValue(sq.getQuest().getTargetValue())
                .rewardXp(sq.getQuest().getRewardXp())
                .rewardCoins(sq.getQuest().getRewardCoins())
                .currentProgress(sq.getCurrentProgress())
                .isClaimed(sq.getIsClaimed())
                .category(sq.getQuest().getCategory())
                .build();
    }
}
