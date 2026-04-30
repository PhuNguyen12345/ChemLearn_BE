package com.example.chemlearn.lab.service.impl;

import com.example.chemlearn.core.response.PageResponse;
import com.example.chemlearn.lab.dto.response.LabSummaryResponse;
import com.example.chemlearn.lab.entity.Lab;
import com.example.chemlearn.lab.enums.LabCategory;
import com.example.chemlearn.lab.enums.LabType;
import com.example.chemlearn.lab.mapper.LabSummaryResponseMapper;
import com.example.chemlearn.lab.repository.LabRepository;
import com.example.chemlearn.lab.service.LabService;
import com.example.chemlearn.lms.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class LabServiceImpl implements LabService {
    private LabRepository labRepository;
    private UserRepository userRepository;
    private LabSummaryResponseMapper labSummaryResponseMapper;

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
            labPage = labRepository.findMySandboxLabs(authorId,keyword, category,pageable);
        }

        else if (type == LabType.ASSIGNMENT) {
            if (!userRepository.existsById(authorId)) {
                throw new RuntimeException("Không tìm thấy người dùng có id tương ứng.");
            }
            labPage = labRepository.findMyAssignmentLabs(authorId, keyword, category, pageable);
        }
        else {
            labPage = labRepository.findPremadeLabs(keyword, category, pageable);
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
}
