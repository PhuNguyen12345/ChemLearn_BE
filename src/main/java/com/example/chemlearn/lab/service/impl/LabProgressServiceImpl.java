package com.example.chemlearn.lab.service.impl;

import com.example.chemlearn.lab.dto.request.SaveProgressRequest;
import com.example.chemlearn.lab.dto.response.LabPlayResponse;
import com.example.chemlearn.lab.entity.Lab;
import com.example.chemlearn.lab.entity.LabConfiguration;
import com.example.chemlearn.lab.entity.UserLabProgress;
import com.example.chemlearn.lab.repository.LabRepository;
import com.example.chemlearn.lab.repository.UserLabProgressRepository;
import com.example.chemlearn.lab.service.LabProgressService;
import com.example.chemlearn.lms.repository.StudentRepository;
import com.example.chemlearn.lms.repository.StudyClassAssignmentRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.example.chemlearn.gamification.service.QuestService;
import com.example.chemlearn.gamification.service.GamificationProfileService;
import com.example.chemlearn.gamification.enums.XpSource;
import com.example.chemlearn.lab.enums.Difficulty;
import com.example.chemlearn.lab.enums.LabType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Slf4j
@Service
@AllArgsConstructor
public class LabProgressServiceImpl implements LabProgressService {
    private LabRepository labRepository;
    private UserLabProgressRepository labProgressRepository;
    private StudentRepository studentRepository;
    private StudyClassAssignmentRepository studyClassAssignmentRepository;
    private QuestService questService;
    private GamificationProfileService gamificationProfileService;

    @Override
    public LabPlayResponse playLab(UUID labId, UUID studentId) {
        //find lab by id
        Lab lab = labRepository.findById(labId).orElseThrow(() -> new RuntimeException("Không tìm thấy bài lab."));
        System.out.println(lab.toString());
        LabConfiguration defaultConfig = lab.getLabConfiguration();

        //Check if save progress
        Optional<UserLabProgress> progressOpt = labProgressRepository.findByStudentIdAndLabId(studentId, labId);

        //Merge logic to decide which workspace to enter
        Object workspaceData;
        Object viewportData = null;
        List<String> completedActions = null;
        Integer currentScore = 0;
        Integer progressPercent = 0;
        String status = "UNCOMPLETED";

        //Check if request for continue progress
        if (progressOpt.isPresent()) {
            UserLabProgress p = progressOpt.get();
            workspaceData = p.getCurrentWorkspace();
            viewportData = p.getViewport();
            completedActions = p.getCompletedActions();
            currentScore = p.getCurrentScore() != null ? p.getCurrentScore() : 0;
            progressPercent = p.getProgressPercent() != null ? p.getProgressPercent() : 0;
            status = p.getStatus() != null ? p.getStatus() : "UNCOMPLETED";
        }
        else {
            //get initial data
            workspaceData = defaultConfig.getInitialWorkspace();
        }
        return LabPlayResponse.builder()
                .labId(labId)
                .title(lab.getTitle())
                .type(lab.getType().toString())
                .workspace(workspaceData)
                .config(defaultConfig.getConfig())
                .currentScore(currentScore)
                .progressPercent(progressPercent)
                .status(status)
                .viewport(viewportData)
                .completedActions(completedActions)
                .build();
    }


    @Override
    @Transactional
    public void saveProgress(UUID studentId, UUID labId, SaveProgressRequest request) {
        //1. Check if progress exists
        Optional<UserLabProgress> existingProgress = labProgressRepository.findByStudentIdAndLabId(studentId, labId);
        UserLabProgress progress;

        //Check if already exist progress
        if (existingProgress.isPresent()) {
            progress = existingProgress.get();
        }
        else {
            //if not exist progress
            progress = new UserLabProgress();
            //Get reference by id to set foreign key
            progress.setStudent(studentRepository.getReferenceById(studentId));
            progress.setLab(labRepository.getReferenceById(labId));
            progress.setStartedAt(Instant.now());
            progress.setProgressPercent(0);
            progress.setCurrentScore(0);
            progress.setIsFinished(false);
        }

        progress.setStatus("IN_PROGRESS");

        //2. Update newest data to frontend
        progress.setCurrentWorkspace(request.getCurrentWorkspace());
        progress.setViewport(request.getViewport());

        if (request.getCompletedActions() != null) {
            progress.setCompletedActions(request.getCompletedActions());
        }

        if (request.getCurrentScore() != null) {
            progress.setCurrentScore(request.getCurrentScore());
        }
        if (request.getProgressPercent() != null) {
            progress.setProgressPercent(request.getProgressPercent());
        }
        if (request.getStatus() != null) {
            progress.setStatus(request.getStatus());
        }

        // --- Logic: Cộng thưởng cho bài Lab PREMADE ---
        if ("COMPLETED".equals(request.getStatus()) && !Boolean.TRUE.equals(progress.getIsFinished())) {
            Lab lab = labRepository.findById(labId).orElse(null);
            if (lab != null && lab.getType() == LabType.PREMADE) {
                // Đánh dấu hoàn thành để không cộng điểm nhiều lần
                progress.setIsFinished(true);
                progress.setSubmittedAt(Instant.now());
                
                // Tính điểm theo độ khó
                int xpReward = 50;
                int coinsReward = 10;
                if (lab.getDifficulty() == Difficulty.MEDIUM) {
                    xpReward = 100;
                    coinsReward = 20;
                } else if (lab.getDifficulty() == Difficulty.HARD) {
                    xpReward = 150;
                    coinsReward = 30;
                }
                
                // Cộng XP và Vàng
                gamificationProfileService.addExpAndCoins(
                        studentId,
                        xpReward,
                        coinsReward,
                        XpSource.LAB,
                        "Hoàn thành bài thực hành: " + lab.getTitle()
                );
                
                // Hoàn thành nhiệm vụ ngày
                try {
                    questService.updateProgress(studentId, "DO_LAB", 1);
                } catch (Exception e) {
                    log.error("Failed to track DO_LAB quest progress", e);
                }
            }
        }

        //set last edited time
        progress.setLastEditedAt(Instant.now());
        UserLabProgress savedProgress = labProgressRepository.save(progress);
        log.info("Saved progress for student {} and lab {}", studentId, labId);
    }

    @Override
    @Transactional
    public void submitLab(UUID studentId, UUID labId) {
        //find current progress
        UserLabProgress progress = labProgressRepository.findByStudentIdAndLabId(studentId, labId).orElseThrow(()
                -> new RuntimeException("Bạn chưa thực hiện bài thí nghiệm này."));
        //check if student already submitted
        if (Boolean.TRUE.equals(progress.getIsFinished())) {
            throw new RuntimeException("Bài thí nghiệm này đã được nộp trước đó.");
        }

        //check if it is an assignment
        if (!studyClassAssignmentRepository.isAssignment(labId, studentId)) {
            throw new RuntimeException("Chỉ có bài tập mới có thể nộp.");
        }

        //final logic
        progress.setIsFinished(true);
        progress.setStatus("COMPLETED");
        progress.setSubmittedAt(Instant.now());
        progress.setLastEditedAt(Instant.now());

        //Hard coding progress and percent
        progress.setProgressPercent(100);
        progress.setCurrentScore(100);

        UserLabProgress submittedProgress = labProgressRepository.save(progress);
        log.info("Submitted progress for student {} and lab {}", studentId, labId);

        // Track DO_LAB daily quest progress
        try {
            questService.updateProgress(studentId, "DO_LAB", 1);
        } catch (Exception e) {
            log.error("Failed to track DO_LAB quest progress", e);
        }
    }

    @Override
    @Transactional
    public void resetLab(UUID studentId, UUID labId) {
        Optional<UserLabProgress> progressOpt = labProgressRepository.findByStudentIdAndLabId(studentId, labId);
        //Check if exist progress
        if (progressOpt.isEmpty()) {
            throw new RuntimeException("Bạn chưa có tiến trình nào ở bài Lab này để xoá.");
        }
        
        UserLabProgress progress = progressOpt.get();
        if (Boolean.TRUE.equals(progress.getIsFinished()) && studyClassAssignmentRepository.isAssignment(labId, studentId)) {
            throw new RuntimeException("Bạn không thể làm lại bài tập đã nộp.");
        }

        labProgressRepository.deleteByStudentIdAndLabId(studentId, labId);
    }
}
