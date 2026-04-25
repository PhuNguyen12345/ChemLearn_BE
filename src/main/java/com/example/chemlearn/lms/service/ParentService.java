package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.dto.parent.ParentAssessmentDTO;
import com.example.chemlearn.lms.dto.parent.ParentChildDTO;
import com.example.chemlearn.lms.dto.parent.ParentChildPerformanceDTO;

import java.util.List;
import java.util.UUID;

public interface ParentService {
    List<ParentChildDTO> getChildren(String parentUsername);

    ParentChildPerformanceDTO getChildPerformance(String parentUsername, UUID childId);

    List<ParentAssessmentDTO> getChildAssessments(String parentUsername, UUID childId);
}

