package com.example.chemlearn.lms.service;

import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.gamification.dto.response.QuestResponse;
import com.example.chemlearn.gamification.entity.Item;
import com.example.chemlearn.gamification.service.QuestService;
import com.example.chemlearn.lms.dto.admin.MailAnnouncementRequestDTO;
import com.example.chemlearn.lms.entity.Lesson;
import com.example.chemlearn.lms.entity.Quiz;
import com.example.chemlearn.lms.entity.StudyClass;
import com.example.chemlearn.lms.entity.StudyClassAssignment;
import com.example.chemlearn.lms.repository.ClassStudentLinkRepository;
import com.example.chemlearn.lms.repository.StudentRepository;
import com.example.chemlearn.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoMailNotificationService {

    private static final int MAX_HIGHLIGHTS = 5;

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ClassStudentLinkRepository classStudentLinkRepository;
    private final QuestService questService;
    private final BiCompanionMessageService biCompanionMessageService;

    @Value("${app.mail.notifications.enabled:true}")
    private boolean notificationsEnabled;

    @Value("${app.mail.automatic.enabled:true}")
    private boolean automaticEnabled;

    @Value("${app.mail.daily-reminder.enabled:false}")
    private boolean dailyReminderEnabled;

    @Value("${app.companion-reminder.enabled:true}")
    private boolean companionReminderEnabled;

    @Value("${app.mail.content-updates.enabled:false}")
    private boolean contentUpdateEmailsEnabled;

    @Value("${app.mail.assignment-notifications.enabled:false}")
    private boolean assignmentEmailsEnabled;

    @Value("${app.mail.shop-updates.enabled:false}")
    private boolean shopUpdateEmailsEnabled;

    @Value("${app.mail.scheduler-zone:Asia/Ho_Chi_Minh}")
    private String schedulerZone;

    public int sendAdminAnnouncement(MailAnnouncementRequestDTO request) {
        if (!notificationsEnabled) {
            log.info("Mail notifications are disabled. Skipping admin announcement: {}", request.getSubject());
            return 0;
        }

        List<UserRole> roles = request.getTargetRoles() == null || request.getTargetRoles().isEmpty()
                ? List.of(UserRole.ROLE_STUDENT)
                : request.getTargetRoles();
        List<User> recipients = userRepository.findByRoleInAndIsActiveTrue(roles);
        String category = hasText(request.getCategory()) ? request.getCategory() : "Thông báo ChemLearn";
        String ctaLabel = hasText(request.getCtaLabel()) ? request.getCtaLabel() : "Mở ChemLearn";
        String ctaUrl = hasText(request.getCtaUrl()) ? request.getCtaUrl() : "/student/home";

        return sendToUsers(
                recipients,
                request.getSubject(),
                category,
                request.getTitle(),
                request.getMessage(),
                request.getHighlights(),
                ctaLabel,
                ctaUrl
        );
    }

    public void notifyGlobalLessonCreated(Lesson lesson) {
        notifyGlobalLesson(lesson, "Bài học mới", "Bài học mới đã có trên ChemLearn");
    }

    public void notifyGlobalLessonUpdated(Lesson lesson) {
        notifyGlobalLesson(lesson, "Cập nhật bài học", "Một bài học vừa được cập nhật");
    }

    public void notifyClassLessonCreated(Lesson lesson) {
        notifyClassLesson(lesson, "Bài học mới trong lớp", "Giáo viên vừa đăng bài học mới");
    }

    public void notifyClassLessonUpdated(Lesson lesson) {
        notifyClassLesson(lesson, "Cập nhật bài học trong lớp", "Giáo viên vừa cập nhật bài học");
    }

    public void notifyQuizPublished(Quiz quiz, StudyClassAssignment assignment) {
        if (!assignmentEmailsEnabled || !shouldSendAutomatic() || !Boolean.TRUE.equals(quiz.getPublished()) || quiz.getStudyClass() == null) {
            return;
        }

        UUID classId = quiz.getStudyClass().getId();
        String className = quiz.getStudyClass().getName();
        String quizTitle = quiz.getTitle();
        String dueDate = formatInstant(assignment.getDueDate() != null ? assignment.getDueDate() : quiz.getEndTime());
        String ctaUrl = "/student/quiz/" + quiz.getId();

        afterCommit(() -> sendToUsers(
                classStudentLinkRepository.findActiveStudentsByClassRoomId(classId),
                "ChemLearn - Quiz mới: " + quizTitle,
                "Quiz mới",
                quizTitle,
                "Lớp " + className + " vừa có bài kiểm tra mới. Hãy vào làm sớm để không sát hạn.",
                List.of(
                        "Lớp: " + className,
                        "Thời lượng: " + displayMinutes(quiz.getDurationMinutes()),
                        dueDate == null ? "Hạn làm bài: chưa đặt" : "Hạn làm bài: " + dueDate
                ),
                "Vào làm quiz",
                ctaUrl
        ));
    }

    public void notifyAssignmentCreated(StudyClassAssignment assignment) {
        if (!assignmentEmailsEnabled || !shouldSendAutomatic() || assignment.getStudyClassField() == null || assignment.getQuiz() == null) {
            return;
        }

        UUID classId = assignment.getStudyClassField().getId();
        String className = assignment.getStudyClassField().getName();
        String assignmentTitle = assignment.getTitle();
        String quizTitle = assignment.getQuiz().getTitle();
        String dueDate = formatInstant(assignment.getDueDate());
        String ctaUrl = "/student/quiz/" + assignment.getQuiz().getId();

        afterCommit(() -> sendToUsers(
                classStudentLinkRepository.findActiveStudentsByClassRoomId(classId),
                "ChemLearn - Bài tập mới: " + assignmentTitle,
                "Bài tập mới",
                assignmentTitle,
                "Giáo viên vừa giao một bài tập mới cho lớp " + className + ".",
                List.of(
                        "Quiz: " + quizTitle,
                        "Lớp: " + className,
                        dueDate == null ? "Hạn nộp: chưa đặt" : "Hạn nộp: " + dueDate
                ),
                "Xem bài tập",
                ctaUrl
        ));
    }

    public void notifyShopItemCreated(Item item) {
        notifyShopItem(item, "Vật phẩm mới trong shop", "Shop ChemLearn vừa có vật phẩm mới");
    }

    public void notifyShopItemUpdated(Item item, Integer previousPrice) {
        String intro = previousPrice != null && item.getPriceCoins() != null && item.getPriceCoins() < previousPrice
                ? "Một vật phẩm trong shop vừa được giảm giá. Đây là lúc tốt để ghé mua bằng coins."
                : "Một vật phẩm trong shop vừa được cập nhật thông tin.";
        notifyShopItem(item, "Cập nhật shop ChemLearn", intro);
    }

    @Scheduled(cron = "${app.mail.daily-reminder.cron:0 0 20 * * *}", zone = "${app.mail.scheduler-zone:Asia/Ho_Chi_Minh}")
    public void sendDailyReminderBatch() {
        if (!automaticEnabled) {
            return;
        }

        boolean eveningReminder = ZonedDateTime.now(resolveZone()).getHour() >= 12;
        int companionSent = companionReminderEnabled
                ? biCompanionMessageService.sendScheduledReminderBatch(eveningReminder)
                : 0;

        if (!notificationsEnabled || !dailyReminderEnabled) {
            log.info("{} companion reminder batch created {} in-app messages. Email reminder skipped.",
                    eveningReminder ? "Evening" : "Morning",
                    companionSent);
            return;
        }

        String subject = eveningReminder
                ? "ChemLearn - Nhắc ôn bài buổi tối"
                : "ChemLearn - Nhiệm vụ hôm nay đang chờ bạn";
        String eyebrow = eveningReminder ? "Evening reminder" : "Daily reminder";
        String title = eveningReminder
                ? "Tổng kết nhẹ trước khi nghỉ nào"
                : "Bắt đầu ngày học mới nào";
        String intro = eveningReminder
                ? "Bi nhắc bạn ôn lại phần đã học hôm nay, nhận thưởng nhiệm vụ nếu đã hoàn thành và chuẩn bị nhịp học cho ngày mai."
                : "ChemLearn đã chuẩn bị nhiệm vụ hằng ngày để bạn luyện tập nhẹ nhàng mà vẫn nhận thưởng đều.";
        String finalHighlight = eveningReminder
                ? "Nếu còn nhiệm vụ chưa nhận thưởng, hãy vào ChemLearn để chốt trước khi kết thúc ngày."
                : "Hoàn thành nhiệm vụ để giữ nhịp học và tích lũy coins cho shop.";

        int sent = 0;
        for (Student student : studentRepository.findActiveStudentsWithUsers()) {
            User user = student.getUsers();
            if (!isDeliverable(user)) {
                continue;
            }

            List<QuestResponse> quests = questService.getDailyQuests(student.getId());
            List<QuestResponse> unfinished = quests.stream()
                    .filter(quest -> !Boolean.TRUE.equals(quest.getIsClaimed()))
                    .toList();

            List<String> highlights = new ArrayList<>();
            highlights.add("Bạn có " + unfinished.size() + " nhiệm vụ hôm nay đang chờ hoàn thành.");
            unfinished.stream()
                    .limit(3)
                    .map(quest -> quest.getTitle() + " - thưởng " + quest.getRewardXp() + " XP và "
                            + nullToZero(quest.getRewardCoins()) + " coins")
                    .forEach(highlights::add);
            highlights.add(finalHighlight);

            emailService.sendNotificationEmail(
                    user.getEmail(),
                    user.getFullName(),
                    subject,
                    eyebrow,
                    title,
                    intro,
                    highlights,
                    "Xem nhiệm vụ",
                    "/student/home"
            );
            sent++;
        }
        log.info("{} reminder batch queued {} emails and created {} in-app messages",
                eveningReminder ? "Evening" : "Morning",
                sent,
                companionSent);
    }

    private void notifyGlobalLesson(Lesson lesson, String eyebrow, String intro) {
        if (!contentUpdateEmailsEnabled || !shouldSendAutomatic() || !Boolean.TRUE.equals(lesson.getPublished())) {
            return;
        }

        String chapterTitle = lesson.getChapter() == null ? "Khu học tập" : lesson.getChapter().getTitle();
        String lessonTitle = lesson.getTitle();
        String ctaUrl = "/student/study-zone";

        afterCommit(() -> sendToUsers(
                userRepository.findByRoleAndIsActiveTrue(UserRole.ROLE_STUDENT),
                "ChemLearn - " + lessonTitle,
                eyebrow,
                lessonTitle,
                intro + ": " + lessonTitle + ".",
                List.of(
                        "Chương: " + chapterTitle,
                        "Thời lượng ước tính: " + displayMinutes(lesson.getDurationMinutes()),
                        "Nội dung đã sẵn sàng trong Khu vực học tập."
                ),
                "Vào học ngay",
                ctaUrl
        ));
    }

    private void notifyClassLesson(Lesson lesson, String eyebrow, String intro) {
        if (!contentUpdateEmailsEnabled || !shouldSendAutomatic() || !Boolean.TRUE.equals(lesson.getPublished()) || lesson.getOwnerClass() == null) {
            return;
        }

        StudyClass ownerClass = lesson.getOwnerClass();
        UUID classId = ownerClass.getId();
        String className = ownerClass.getName();
        String chapterTitle = lesson.getChapter() == null ? "Tài liệu lớp" : lesson.getChapter().getTitle();
        String lessonTitle = lesson.getTitle();
        String ctaUrl = "/student/class/" + classId + "/material/" + lesson.getId();

        afterCommit(() -> sendToUsers(
                classStudentLinkRepository.findActiveStudentsByClassRoomId(classId),
                "ChemLearn - " + lessonTitle,
                eyebrow,
                lessonTitle,
                intro + " trong lớp " + className + ".",
                List.of(
                        "Lớp: " + className,
                        "Chương: " + chapterTitle,
                        "Thời lượng ước tính: " + displayMinutes(lesson.getDurationMinutes())
                ),
                "Mở bài học",
                ctaUrl
        ));
    }

    private void notifyShopItem(Item item, String eyebrow, String intro) {
        if (!shopUpdateEmailsEnabled || !shouldSendAutomatic()) {
            return;
        }

        String itemName = item.getName();
        List<String> highlights = new ArrayList<>();
        highlights.add("Vật phẩm: " + itemName);
        highlights.add("Giá hiện tại: " + nullToZero(item.getPriceCoins()) + " coins");
        if (hasText(item.getDescription())) {
            highlights.add(item.getDescription());
        }

        afterCommit(() -> sendToUsers(
                userRepository.findByRoleAndIsActiveTrue(UserRole.ROLE_STUDENT),
                "ChemLearn - " + eyebrow,
                "Shop ChemLearn",
                itemName,
                intro,
                highlights,
                "Ghé shop",
                "/student/shop"
        ));
    }

    private int sendToUsers(
            List<User> recipients,
            String subject,
            String eyebrow,
            String title,
            String intro,
            List<String> highlights,
            String ctaLabel,
            String ctaUrl
    ) {
        int sent = 0;
        for (User user : uniqueDeliverableUsers(recipients)) {
            emailService.sendNotificationEmail(
                    user.getEmail(),
                    user.getFullName(),
                    subject,
                    eyebrow,
                    title,
                    intro,
                    limitHighlights(highlights),
                    ctaLabel,
                    ctaUrl
            );
            sent++;
        }
        log.info("Queued {} notification emails for '{}'", sent, subject);
        return sent;
    }

    private List<User> uniqueDeliverableUsers(List<User> users) {
        Map<UUID, User> unique = new LinkedHashMap<>();
        if (users == null) {
            return List.of();
        }
        for (User user : users) {
            if (isDeliverable(user)) {
                unique.put(user.getId(), user);
            }
        }
        return new ArrayList<>(unique.values());
    }

    private boolean isDeliverable(User user) {
        return user != null
                && Boolean.TRUE.equals(user.getIsActive())
                && hasText(user.getEmail())
                && !user.getEmail().endsWith("@chemlearn.local");
    }

    private List<String> limitHighlights(List<String> highlights) {
        if (highlights == null) {
            return List.of();
        }
        return highlights.stream()
                .filter(this::hasText)
                .limit(MAX_HIGHLIGHTS)
                .toList();
    }

    private boolean shouldSendAutomatic() {
        return notificationsEnabled && automaticEnabled;
    }

    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }
        action.run();
    }

    private String displayMinutes(Integer minutes) {
        if (minutes == null || minutes <= 0) {
            return "linh hoạt";
        }
        return minutes + " phút";
    }

    private String formatInstant(Instant instant) {
        if (instant == null) {
            return null;
        }
        return DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
                .withZone(resolveZone())
                .format(instant);
    }

    private ZoneId resolveZone() {
        try {
            return ZoneId.of(schedulerZone);
        } catch (Exception ex) {
            return ZoneId.of("Asia/Ho_Chi_Minh");
        }
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
