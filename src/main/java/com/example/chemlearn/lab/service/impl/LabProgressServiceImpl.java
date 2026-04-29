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
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
@Slf4j
@Service
@AllArgsConstructor
public class LabProgressServiceImpl implements LabProgressService {
    private LabRepository labRepository;
    private UserLabProgressRepository labProgressRepository;
    private StudentRepository studentRepository;

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
        //Check if request for continue progress
        if (progressOpt.isPresent()) {
            workspaceData = progressOpt.get().getCurrentWorkspace();
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

        //2. Update newest data to frontend
        progress.setCurrentWorkspace(request.getCurrentWorkspace());
        progress.setViewport(request.getViewport());

        if (request.getCompletedActions() != null) {
            progress.setCompletedActions(request.getCompletedActions());
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
    }

    @Override
    @Transactional
    public void resetLab(UUID studentId, UUID labId) {
        boolean isExistProgress =  labProgressRepository.findByStudentIdAndLabId(studentId, labId).isPresent();
        //Check if exist progress
        if (!isExistProgress) {
            throw new RuntimeException("Bạn chưa có tiến trình nào ở bài Lab này để xoá.");
        }
        labProgressRepository.deleteByStudentIdAndLabId(studentId, labId);
    }
}
