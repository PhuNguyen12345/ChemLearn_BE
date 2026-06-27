package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.dto.companion.BiCompanionMessageResponseDTO;

import java.util.List;
import java.util.UUID;

public interface BiCompanionMessageService {
    List<BiCompanionMessageResponseDTO> findMessagesForStudent(String username);

    int sendScheduledReminderBatch(boolean eveningReminder);

    BiCompanionMessageResponseDTO sendAdminMessageToStudent(UUID studentId, String title, String message);
}
