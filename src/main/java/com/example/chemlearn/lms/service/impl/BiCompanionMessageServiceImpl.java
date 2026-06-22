package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.gamification.dto.response.QuestResponse;
import com.example.chemlearn.gamification.service.QuestService;
import com.example.chemlearn.lms.dto.companion.BiCompanionMessageResponseDTO;
import com.example.chemlearn.lms.entity.BiCompanionMessage;
import com.example.chemlearn.lms.exception.CustomExceptions;
import com.example.chemlearn.lms.repository.BiCompanionMessageRepository;
import com.example.chemlearn.lms.repository.StudentRepository;
import com.example.chemlearn.lms.service.BiCompanionMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BiCompanionMessageServiceImpl implements BiCompanionMessageService {
    private static final String MORNING_TYPE = "MORNING_REMINDER";
    private static final String EVENING_TYPE = "EVENING_REMINDER";
    private static final String SENDER_NAME = "Admin ChemLearn";

    private final BiCompanionMessageRepository biCompanionMessageRepository;
    private final StudentRepository studentRepository;
    private final QuestService questService;

    @Value("${app.mail.scheduler-zone:Asia/Ho_Chi_Minh}")
    private String schedulerZone;

    @Override
    @Transactional(readOnly = true)
    public List<BiCompanionMessageResponseDTO> findMessagesForStudent(String username) {
        Student student = studentRepository.findByUsers_Username(username)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Student not found"));

        List<BiCompanionMessage> messages =
                new ArrayList<>(biCompanionMessageRepository.findTop30ByStudent_IdOrderByCreatedAtDesc(student.getId()));
        Collections.reverse(messages);

        return messages.stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public int sendScheduledReminderBatch(boolean eveningReminder) {
        LocalDate today = LocalDate.now(resolveZone());
        String messageType = eveningReminder ? EVENING_TYPE : MORNING_TYPE;
        int sent = 0;

        for (Student student : studentRepository.findActiveStudentsWithUsers()) {
            if (biCompanionMessageRepository.existsByStudent_IdAndMessageTypeAndScheduledFor(
                    student.getId(),
                    messageType,
                    today
            )) {
                continue;
            }

            try {
                BiCompanionMessage message = buildScheduledMessage(student, today, messageType, eveningReminder);
                biCompanionMessageRepository.save(message);
                sent++;
            } catch (DataIntegrityViolationException ex) {
                log.debug("Skipped duplicate Bi companion message for student {} and slot {}", student.getId(), messageType);
            } catch (Exception ex) {
                log.warn("Could not create Bi companion message for student {}", student.getId(), ex);
            }
        }

        return sent;
    }

    @Override
    @Transactional
    public BiCompanionMessageResponseDTO sendAdminMessageToStudent(UUID studentId, String title, String messageText) {
        Student student = studentRepository.findByUsers_Id(studentId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Student not found"));

        BiCompanionMessage message = new BiCompanionMessage();
        message.setStudent(student);
        message.setSenderName(SENDER_NAME);
        message.setTitle(title.trim());
        message.setMessage(messageText.trim());
        message.setMessageType("ADMIN_REPLY");
        message.setScheduledFor(LocalDate.now(resolveZone()));

        return toDto(biCompanionMessageRepository.save(message));
    }

    private BiCompanionMessage buildScheduledMessage(
            Student student,
            LocalDate scheduledFor,
            String messageType,
            boolean eveningReminder
    ) {
        QuestSummary questSummary = buildQuestSummary(student);
        String studentName = resolveStudentName(student);

        BiCompanionMessage message = new BiCompanionMessage();
        message.setStudent(student);
        message.setSenderName(SENDER_NAME);
        message.setMessageType(messageType);
        message.setScheduledFor(scheduledFor);

        if (eveningReminder) {
            message.setTitle("8:00 tối - Admin nhắn Bi cùng bạn ôn lại");
            message.setMessage("""
                    Chào %s, admin nhờ Bi ghé nhắc bạn một chút nè.
                    Hôm nay bạn còn %d nhiệm vụ chưa nhận thưởng. %s
                    Dù ngày học có bận hay mệt, chỉ cần ôn lại một ý nhỏ cũng là đang tiến lên. Bi và ChemLearn vẫn đồng hành cùng bạn.
                    """.formatted(studentName, questSummary.unfinishedCount(), questSummary.highlight()));
        } else {
            message.setTitle("7:00 sáng - Admin gửi lời khởi động");
            message.setMessage("""
                    Chào %s, chúc bạn một ngày học thật nhẹ nhàng và rõ ràng.
                    Hôm nay ChemLearn có %d nhiệm vụ đang chờ bạn. %s
                    Hãy bắt đầu bằng một việc nhỏ: giữ streak, xem nhiệm vụ hằng ngày, rồi chọn một bài ngắn để khởi động nhé. Admin và Bi tin bạn làm được.
                    """.formatted(studentName, questSummary.unfinishedCount(), questSummary.highlight()));
        }

        return message;
    }

    private QuestSummary buildQuestSummary(Student student) {
        try {
            List<QuestResponse> quests = questService.getDailyQuests(student.getId());
            List<QuestResponse> unfinished = quests.stream()
                    .filter(quest -> !Boolean.TRUE.equals(quest.getIsClaimed()))
                    .toList();
            String highlight = unfinished.stream()
                    .findFirst()
                    .map(quest -> "Gợi ý trước: " + quest.getTitle() + ".")
                    .orElse("Nếu đã xong hết, hãy dành vài phút ôn lại phần bạn thấy khó nhất.");
            return new QuestSummary(unfinished.size(), highlight);
        } catch (Exception ex) {
            log.debug("Could not build quest summary for student {}", student.getId(), ex);
            return new QuestSummary(0, "Bạn có thể mở trang chủ để xem việc nên làm tiếp theo.");
        }
    }

    private String resolveStudentName(Student student) {
        User user = student.getUsers();
        if (user == null || user.getFullName() == null || user.getFullName().isBlank()) {
            return "bạn";
        }
        return user.getFullName().trim();
    }

    private BiCompanionMessageResponseDTO toDto(BiCompanionMessage message) {
        return BiCompanionMessageResponseDTO.builder()
                .id(message.getId())
                .senderName(message.getSenderName())
                .title(message.getTitle())
                .message(message.getMessage())
                .messageType(message.getMessageType())
                .scheduledFor(message.getScheduledFor())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .build();
    }

    private ZoneId resolveZone() {
        try {
            return ZoneId.of(schedulerZone);
        } catch (Exception ex) {
            return ZoneId.of("Asia/Ho_Chi_Minh");
        }
    }

    private record QuestSummary(int unfinishedCount, String highlight) {
    }
}
