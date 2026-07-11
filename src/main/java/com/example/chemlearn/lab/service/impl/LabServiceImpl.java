package com.example.chemlearn.lab.service.impl;

import com.example.chemlearn.core.response.PageResponse;
import com.example.chemlearn.lab.dto.response.LabSummaryResponse;
import com.example.chemlearn.lab.entity.Lab;
import com.example.chemlearn.lab.entity.LabConfiguration;
import com.example.chemlearn.lab.entity.UserLabProgress;
import com.example.chemlearn.lab.enums.LabCategory;
import com.example.chemlearn.lab.enums.LabType;
import com.example.chemlearn.lab.mapper.LabSummaryResponseMapper;
import com.example.chemlearn.lab.repository.LabConfigurationRepository;
import com.example.chemlearn.lab.repository.LabRepository;
import com.example.chemlearn.lab.repository.UserLabProgressRepository;
import com.example.chemlearn.lab.service.LabService;
import com.example.chemlearn.lms.repository.StudentRepository;
import com.example.chemlearn.lms.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class LabServiceImpl implements LabService {
    private LabRepository labRepository;
    private UserRepository userRepository;
    private LabSummaryResponseMapper labSummaryResponseMapper;
    private LabConfigurationRepository labConfigRepository;
    private StudentRepository studentRepository;
    private UserLabProgressRepository progressRepository;

    @Override
    public PageResponse<LabSummaryResponse> findLabs(UUID authorId, String keyword, LabCategory category, LabType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Lab> labPage;

        //Check if lab is SANDBOX type
        if (type == LabType.SANDBOX) {
            //Check user id
            if (!userRepository.existsById(authorId)) {
                throw new RuntimeException("Không tìm thấy người dùng có id tương ứng.");
            }
            labPage = labRepository.findMySandboxLabs(LabType.SANDBOX, authorId, keyword, category, pageable);
        }

        else if (type == LabType.ASSIGNMENT) {
            if (!userRepository.existsById(authorId)) {
                throw new RuntimeException("Không tìm thấy người dùng có id tương ứng.");
            }
            labPage = labRepository.findMyAssignmentLabs(authorId, keyword, category, pageable);
        }
        else {
            labPage = labRepository.findPremadeLabs(LabType.PREMADE, keyword, category, pageable);
        }
        List<Lab> pageContent = labPage.getContent();
        List<LabSummaryResponse> responseContent = labSummaryResponseMapper.toDtoList(pageContent);

        return PageResponse.<LabSummaryResponse>builder()
                .content(responseContent)
                .size(labPage.getSize())
                .page(labPage.getNumber())
                .totalPages(labPage.getTotalPages())
                .totalElements(labPage.getTotalElements())
                .build();
    }

    @Override
    @Transactional
    public void renameSandboxLab(UUID authorId, UUID labId, String newTitle) {
        //find lab
        Lab lab = labRepository.findById(labId).orElseThrow(()
                -> new RuntimeException("Không tìm thấy bài lab tương ứng."));
        //Check if this is SANDBOX and author is current user
        if (!"SANDBOX".equals(lab.getType().name())) {
            throw new RuntimeException("Bạn chỉ có thể đổi tên bài lab chế độ SANDBOX.");
        }
        if (!lab.getAuthorId().equals(authorId)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa bài lab này.");
        }
        //update name
        lab.setTitle(newTitle);
        labRepository.save(lab);
    }

    @Override
    public UUID createSandboxLab(UUID authorId) {
        Lab sandboxLab = new Lab();
        sandboxLab.setTitle("Phòng thì nghiệm "+ LocalDateTime.now());
        sandboxLab.setAuthorId(authorId);
        sandboxLab.setType(LabType.SANDBOX);
        sandboxLab.setDescription("Phòng thí nghiệm tự do của bạn. Hãy thoả sức sáng tạo.");
        sandboxLab.setCategory(LabCategory.GENERAL);
        sandboxLab.setMaxScore(0);
        Lab savedLab  = labRepository.save(sandboxLab);

        LabConfiguration labConfig = new LabConfiguration();
        labConfig.setLab(savedLab);
        labConfig.setConfig(new HashMap<>());

        Map<String, Object> viewport = new HashMap<>();
        viewport.put("zoom_scale", 1.0);
        viewport.put("offset", Map.of("x",0,"y",0));
        labConfig.setViewport(viewport);
        labConfig.setInitialWorkspace(new ArrayList<>());
        labConfigRepository.save(labConfig);

        //show immediately in list of sandbox labs
        UserLabProgress progress = new UserLabProgress();
        progress.setLab(savedLab);
        progress.setStudent(studentRepository.getReferenceById(authorId)); //call proxy
        progress.setStatus("IN_PROGRESS");
        progress.setProgressPercent(0);
        progress.setCurrentScore(0);
        progress.setCompletedActions(new ArrayList<>());
        progress.setCurrentWorkspace(new ArrayList<>());
        progress.setViewport(viewport);
        progressRepository.save(progress);

        return savedLab.getId();
    }
}
