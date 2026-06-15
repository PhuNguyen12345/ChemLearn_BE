package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.gamification.enums.ItemType;
import com.example.chemlearn.gamification.repository.ItemRepository;
import com.example.chemlearn.gamification.repository.MapIslandRepository;
import com.example.chemlearn.gamification.repository.MapNodeRepository;
import com.example.chemlearn.gamification.repository.PetSpeciesRepository;
import com.example.chemlearn.gamification.repository.StudentNodeProgressRepository;
import com.example.chemlearn.gamification.repository.StudentPetRepository;
import com.example.chemlearn.lab.repository.LabRepository;
import com.example.chemlearn.lab.repository.UserLabProgressRepository;
import com.example.chemlearn.lms.dto.admin.AdminDashboardSummaryDTO;
import com.example.chemlearn.lms.repository.ChapterRepository;
import com.example.chemlearn.lms.repository.FeedbackReportRepository;
import com.example.chemlearn.lms.repository.LessonProgressRepository;
import com.example.chemlearn.lms.repository.LessonRepository;
import com.example.chemlearn.lms.repository.UserRepository;
import com.example.chemlearn.lms.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {
    private final UserRepository userRepository;
    private final ChapterRepository chapterRepository;
    private final LessonRepository lessonRepository;
    private final LabRepository labRepository;
    private final UserLabProgressRepository userLabProgressRepository;
    private final MapIslandRepository mapIslandRepository;
    private final MapNodeRepository mapNodeRepository;
    private final StudentNodeProgressRepository studentNodeProgressRepository;
    private final PetSpeciesRepository petSpeciesRepository;
    private final StudentPetRepository studentPetRepository;
    private final ItemRepository itemRepository;
    private final FeedbackReportRepository feedbackReportRepository;
    private final LessonProgressRepository lessonProgressRepository;

    @Override
    public AdminDashboardSummaryDTO getSummary() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActiveTrue();
        long students = userRepository.countByRole(UserRole.ROLE_STUDENT);
        long teachers = userRepository.countByRole(UserRole.ROLE_TEACHER);
        long parents = userRepository.countByRole(UserRole.ROLE_PARENT);
        long chapters = chapterRepository.count();
        long lessons = lessonRepository.count();
        long labs = labRepository.count();
        long labProgress = userLabProgressRepository.count();
        long islands = mapIslandRepository.count();
        long nodes = mapNodeRepository.count();
        long nodeProgress = studentNodeProgressRepository.count();
        long petSpecies = petSpeciesRepository.count();
        long studentPets = studentPetRepository.count();
        long eggItems = itemRepository.countByItemType(ItemType.EGG);
        long totalReports = feedbackReportRepository.count();
        long openReports = feedbackReportRepository.countByStatusNot("RESOLVED");
        long completedLessonProgress = lessonProgressRepository.countByIsCompleted(true);
        long inProgressLessonProgress = Math.max(lessonProgressRepository.count() - completedLessonProgress, 0);

        return AdminDashboardSummaryDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .students(students)
                .teachers(teachers)
                .parents(parents)
                .chapters(chapters)
                .lessons(lessons)
                .labs(labs)
                .labProgressRecords(labProgress)
                .mapIslands(islands)
                .mapNodes(nodes)
                .nodeProgressRecords(nodeProgress)
                .petSpecies(petSpecies)
                .studentPets(studentPets)
                .eggItems(eggItems)
                .openReports(openReports)
                .totalReports(totalReports)
                .completedLessonProgress(completedLessonProgress)
                .inProgressLessonProgress(inProgressLessonProgress)
                .moduleUsage(List.of(
                        module("Progress map", nodes, nodeProgress),
                        module("Lab ảo", labs, labProgress),
                        module("Bài học", lessons, lessonProgressRepository.count()),
                        module("Pet", petSpecies, studentPets)
                ))
                .roleBreakdown(List.of(
                        role("Student", students),
                        role("Teacher", teachers),
                        role("Parent", parents),
                        role("Admin", userRepository.countByRole(UserRole.ROLE_ADMIN))
                ))
                .build();
    }

    private AdminDashboardSummaryDTO.ModuleUsageDTO module(String name, long total, long activity) {
        int health = total <= 0 ? 0 : (int) Math.min(100, Math.round((activity * 100.0) / Math.max(total, 1)));
        return AdminDashboardSummaryDTO.ModuleUsageDTO.builder()
                .name(name)
                .total(total)
                .activity(activity)
                .health(health)
                .build();
    }

    private AdminDashboardSummaryDTO.RoleBreakdownDTO role(String role, long count) {
        return AdminDashboardSummaryDTO.RoleBreakdownDTO.builder()
                .role(role)
                .count(count)
                .build();
    }
}
