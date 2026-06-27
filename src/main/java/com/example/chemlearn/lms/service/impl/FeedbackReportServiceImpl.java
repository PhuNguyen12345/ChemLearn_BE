package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.lms.dto.feedback.FeedbackReportRequestDTO;
import com.example.chemlearn.lms.dto.feedback.FeedbackReportResponseDTO;
import com.example.chemlearn.lms.dto.feedback.FeedbackReportStatusUpdateDTO;
import com.example.chemlearn.lms.entity.FeedbackReport;
import com.example.chemlearn.lms.exception.CustomExceptions;
import com.example.chemlearn.lms.repository.FeedbackReportRepository;
import com.example.chemlearn.lms.repository.UserRepository;
import com.example.chemlearn.lms.service.FeedbackReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedbackReportServiceImpl implements FeedbackReportService {
    private final FeedbackReportRepository feedbackReportRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public FeedbackReportResponseDTO create(String username, FeedbackReportRequestDTO dto) {
        User reporter = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomExceptions.UnauthorizedException("User not found"));

        FeedbackReport report = new FeedbackReport();
        report.setReporter(reporter);
        report.setType(normalize(dto.getType()));
        report.setPriority(normalize(dto.getPriority()));
        report.setTitle(dto.getTitle().trim());
        report.setMessage(dto.getMessage().trim());
        report.setStatus("OPEN");
        return toDto(feedbackReportRepository.save(report));
    }

    @Override
    public List<FeedbackReportResponseDTO> findAll() {
        return feedbackReportRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public FeedbackReportResponseDTO updateStatus(UUID reportId, FeedbackReportStatusUpdateDTO dto) {
        FeedbackReport report = feedbackReportRepository.findById(reportId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Feedback report not found"));
        report.setStatus(normalize(dto.getStatus()));
        report.setAdminNote(dto.getAdminNote());
        return toDto(feedbackReportRepository.save(report));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.US);
    }

    private FeedbackReportResponseDTO toDto(FeedbackReport report) {
        User reporter = report.getReporter();
        return FeedbackReportResponseDTO.builder()
                .id(report.getId())
                .reporterId(reporter != null ? reporter.getId() : null)
                .reporterName(reporter != null ? reporter.getFullName() : "Unknown")
                .reporterEmail(reporter != null ? reporter.getEmail() : null)
                .type(report.getType())
                .priority(report.getPriority())
                .title(report.getTitle())
                .message(report.getMessage())
                .status(report.getStatus())
                .adminNote(report.getAdminNote())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}
