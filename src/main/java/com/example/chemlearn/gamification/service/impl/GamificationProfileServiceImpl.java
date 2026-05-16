package com.example.chemlearn.gamification.service.impl;

import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.gamification.dto.response.GamificationProfileResponse;
import com.example.chemlearn.gamification.entity.XpLog;
import com.example.chemlearn.gamification.enums.XpSource;
import com.example.chemlearn.gamification.repository.XpLogRepository;
import com.example.chemlearn.gamification.service.GamificationProfileService;
import com.example.chemlearn.lms.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GamificationProfileServiceImpl implements GamificationProfileService {

    private final StudentRepository studentRepository;
    private final XpLogRepository xpLogRepository;

    @Override
    public GamificationProfileResponse getProfile(UUID studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        int experience = student.getExperience() != null ? student.getExperience() : 0;
        int level = calculateLevel(experience);

        return GamificationProfileResponse.builder()
                .experience(experience)
                .level(level)
                .currentStreak(student.getCurrentStreak() != null ? student.getCurrentStreak() : 0)
                .coins(student.getCoins() != null ? student.getCoins() : 0)
                .build();
    }

    @Override
    @Transactional
    public void updateStreak(UUID studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        LocalDate today = LocalDate.now();
        LocalDate lastActive = student.getLastActiveDate();

        if (lastActive == null) {
            student.setCurrentStreak(1);
            student.setLastActiveDate(today);
        } else if (lastActive.isEqual(today.minusDays(1))) {
            // Consecutive day
            student.setCurrentStreak((student.getCurrentStreak() != null ? student.getCurrentStreak() : 0) + 1);
            student.setLastActiveDate(today);
            
            // Give some streak rewards maybe? Optional.
        } else if (lastActive.isBefore(today.minusDays(1))) {
            // Streak broken
            student.setCurrentStreak(1);
            student.setLastActiveDate(today);
        }
        // If lastActive == today, do nothing.

        studentRepository.save(student);
    }

    @Override
    @Transactional
    public void addExpAndCoins(UUID studentId, int expAmount, int coinsAmount, XpSource source, String description) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (expAmount > 0) {
            student.setExperience((student.getExperience() != null ? student.getExperience() : 0) + expAmount);
            // Log XP
            XpLog xpLog = new XpLog();
            xpLog.setStudent(student);
            xpLog.setAmount(expAmount);
            xpLog.setSource(source);
            xpLog.setDescription(description);
            xpLogRepository.save(xpLog);
        }

        if (coinsAmount > 0) {
            student.setCoins((student.getCoins() != null ? student.getCoins() : 0) + coinsAmount);
        }

        studentRepository.save(student);
    }

    private int calculateLevel(int experience) {
        // First level is 1000 exp, go up with each level till level 10 (10000 exp)
        int level = (experience / 1000) + 1;
        return Math.min(level, 10); // Cap at level 10 as requested
    }
}
