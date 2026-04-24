package com.example.chemlearn.service;

import com.example.chemlearn.dtos.parent.ParentAssessmentDTO;
import com.example.chemlearn.dtos.parent.ParentChildDTO;
import com.example.chemlearn.dtos.parent.ParentChildPerformanceDTO;

import java.util.List;

public interface ParentService {
    List<ParentChildDTO> getChildren(String parentUsername);

    ParentChildPerformanceDTO getChildPerformance(String parentUsername, Long childId);

    List<ParentAssessmentDTO> getChildAssessments(String parentUsername, Long childId);
}
