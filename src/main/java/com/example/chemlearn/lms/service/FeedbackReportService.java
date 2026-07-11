package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.dto.feedback.FeedbackReportRequestDTO;
import com.example.chemlearn.lms.dto.feedback.FeedbackReportResponseDTO;
import com.example.chemlearn.lms.dto.feedback.FeedbackReportStatusUpdateDTO;

import java.util.List;
import java.util.UUID;

public interface FeedbackReportService {
    FeedbackReportResponseDTO create(String username, FeedbackReportRequestDTO dto);
    List<FeedbackReportResponseDTO> findAll();
    FeedbackReportResponseDTO updateStatus(UUID reportId, FeedbackReportStatusUpdateDTO dto);
}
