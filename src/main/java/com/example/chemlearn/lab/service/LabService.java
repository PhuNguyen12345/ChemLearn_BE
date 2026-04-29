package com.example.chemlearn.lab.service;

import com.example.chemlearn.core.response.PageResponse;
import com.example.chemlearn.lab.dto.response.LabSummaryResponse;
import com.example.chemlearn.lab.enums.LabCategory;
import com.example.chemlearn.lab.enums.LabType;

import java.util.UUID;

public interface LabService {
    PageResponse<LabSummaryResponse> findLabs(UUID authorId, String keyword, LabCategory category, LabType type, int page, int size);
}
