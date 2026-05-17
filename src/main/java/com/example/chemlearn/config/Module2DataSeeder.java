package com.example.chemlearn.config;

import com.example.chemlearn.core.entity.Parent;
import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.core.entity.Teacher;
import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.lms.entity.*;
import com.example.chemlearn.lms.enums.AssignmentStatus;
import com.example.chemlearn.lms.enums.QuizType;
import com.example.chemlearn.lms.repository.*;
import com.example.chemlearn.gamification.entity.MapIsland;
import com.example.chemlearn.gamification.entity.MapNode;
import com.example.chemlearn.gamification.repository.MapIslandRepository;
import com.example.chemlearn.gamification.repository.MapNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

import static com.example.chemlearn.util.PasswordUtil.hash;

@Configuration
@RequiredArgsConstructor
public class Module2DataSeeder {

    private final UserRepository accountRepository;
    private final ChapterRepository chapterRepository;
    private final LessonRepository lessonRepository;
    private final MiniQuizQuestionRepository miniQuizQuestionRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final AssignmentRepository assignmentRepository;
    private final ParentStudentLinkRepository parentStudentLinkRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final MapIslandRepository mapIslandRepository;
    private final MapNodeRepository mapNodeRepository;
    private final QuestionBankItemRepository questionBankItemRepository;

    @Bean
    public CommandLineRunner seedModule2Data() {
        return args -> {
            // ── Admin ──────────────────────────────────────────────────────────
            accountRepository.findByUsername("admin")
                    .orElseGet(() -> {
                        User account = new User();
                        account.setUsername("admin");
                        account.setFullName("System Admin");
                        account.setEmail("admin@chemlearn.local");
                        account.setPassword(hash("1231"));
                        account.setRole(UserRole.ROLE_ADMIN);
                        account.setIsActive(true);
                        return accountRepository.save(account);
                    });

            // ── Parent: Nguyen Van An ──────────────────────────────────────────
            User parentUser = accountRepository.findByUsername("parent1")
                    .orElseGet(() -> {
                        User u = new User();
                        u.setUsername("parent1");
                        u.setFullName("Nguyễn Văn An");
                        u.setEmail("parent1@chemlearn.local");
                        u.setPassword(hash("123456"));
                        u.setRole(UserRole.ROLE_PARENT);
                        u.setIsActive(true);
                        u = accountRepository.save(u);

                        Parent parent = new Parent();
                        parent.setUsers(u);
                        parent.setPhoneNumber("0901234567");
                        parent.setJobTitle("Kỹ sư");
                        parentRepository.save(parent);

                        return u;
                    });

            // ── Parent: Thor (For testing) ─────────────────────────────────────
            User thorParent = accountRepository.findByUsername("thor")
                    .orElseGet(() -> {
                        User u = new User();
                        u.setUsername("thor");
                        u.setFullName("Thần Sấm Thor");
                        u.setEmail("thor123deptrai@gmail.com");
                        u.setPassword(hash("123456"));
                        u.setRole(UserRole.ROLE_PARENT);
                        u.setIsActive(true);
                        u = accountRepository.save(u);

                        Parent parent = new Parent();
                        parent.setUsers(u);
                        parent.setPhoneNumber("0909090909");
                        parent.setJobTitle("Siêu anh hùng");
                        parentRepository.save(parent);

                        return u;
                    });


            // ── Student 2: Nguyen Thi Bich (new child) ────────────────────────
            User student2User = accountRepository.findByUsername("student2")
                    .orElseGet(() -> {
                        User u = new User();
                        u.setUsername("student2");
                        u.setFullName("Nguyễn Thị Bích");
                        u.setEmail("student2@chemlearn.local");
                        u.setPassword(hash("123456"));
                        u.setRole(UserRole.ROLE_STUDENT);
                        u.setIsActive(true);
                        u = accountRepository.save(u);

                        Student s = new Student();
                        s.setUsers(u);
                        s.setGradeLevel(10);
                        s.setExperience(120);
                        s.setCurrentStreak(3);
                        s.setCoins(50);
                        s.setPvpWins(0);
                        s.setSchoolName("THPT Nguyễn Huệ");
                        studentRepository.save(s);

                        return u;
                    });

            // ── Student 3: Peter (For testing) ────────────────────────────────
            User student3User = accountRepository.findByUsername("peter")
                    .orElseGet(() -> {
                        User u = new User();
                        u.setUsername("peter");
                        u.setFullName("Peter Parker");
                        u.setEmail("peter@chemlearn.local");
                        u.setPassword(hash("123456"));
                        u.setRole(UserRole.ROLE_STUDENT);
                        u.setIsActive(true);
                        u = accountRepository.save(u);

                        Student s = new Student();
                        s.setUsers(u);
                        s.setGradeLevel(11);
                        s.setExperience(300);
                        s.setCurrentStreak(5);
                        s.setCoins(150);
                        s.setPvpWins(10);
                        s.setSchoolName("Trường THPT X-Men");
                        studentRepository.save(s);

                        return u;
                    });

            // ── Link parent → existing test student (username: student) ────────
            accountRepository.findByUsername("student").ifPresent(existingStudentUser -> {
                // Link in ParentStudentLink table
                parentStudentLinkRepository
                        .findByParent_IdAndStudent_Id(parentUser.getId(), existingStudentUser.getId())
                        .orElseGet(() -> {
                            ParentStudentLink link = new ParentStudentLink();
                            link.setParent(parentUser);
                            link.setStudent(existingStudentUser);
                            return parentStudentLinkRepository.save(link);
                        });
                
                // Also link in Student entity (core relationship)
                studentRepository.findById(existingStudentUser.getId()).ifPresent(student -> {
                    parentRepository.findById(parentUser.getId()).ifPresent(parent -> {
                        student.setParent(parent);
                        studentRepository.save(student);
                    });
                });
            });

            // ── Link parent → new student2 ─────────────────────────────────────
            // Link in ParentStudentLink table
            parentStudentLinkRepository
                    .findByParent_IdAndStudent_Id(parentUser.getId(), student2User.getId())
                    .orElseGet(() -> {
                        ParentStudentLink link = new ParentStudentLink();
                        link.setParent(parentUser);
                        link.setStudent(student2User);
                        return parentStudentLinkRepository.save(link);
                    });

            // Also link in Student entity (core relationship)
            studentRepository.findById(student2User.getId()).ifPresent(student -> {
                parentRepository.findById(parentUser.getId()).ifPresent(parent -> {
                    student.setParent(parent);
                    studentRepository.save(student);
                });
            });

            // ── PVP Questions ──────────────────────────────────────────────────
            if (questionBankItemRepository.count() == 0) {
                User admin = accountRepository.findByUsername("admin").orElse(null);
                if (admin != null) {
                    seedQuestion(admin, "H2O là công thức hóa học của chất nào?", "Nước", "Oxy", "Hydro", "Muối ăn", "A");
                    seedQuestion(admin, "Nguyên tố nào có ký hiệu là Na?", "Natri", "Nitơ", "Kẽm", "Đồng", "A");
                    seedQuestion(admin, "Axit sunfuric có công thức là gì?", "H2SO4", "HCl", "HNO3", "H2CO3", "A");
                    seedQuestion(admin, "Kim loại nào ở trạng thái lỏng ở nhiệt độ phòng?", "Thủy ngân", "Sắt", "Chì", "Nhôm", "A");
                    seedQuestion(admin, "Khí nào duy trì sự cháy?", "Khí Oxy", "Khí Cacbonic", "Khí Nitơ", "Khí Hidro", "A");
                }
            }

            // ── Game Map: Elemental Archipelago ───────────────────────────────
            if (mapIslandRepository.count() == 0) {
                // Island 1
                MapIsland island1 = new MapIsland();
                island1.setName("Quần đảo Nhập môn");
                island1.setDescription("Nơi bắt đầu hành trình khám phá thế giới nguyên tử.");
                island1.setOrderIndex(1);
                island1.setUnlockLevel(1);
                island1 = mapIslandRepository.save(island1);

                seedNodes(island1, "Cấu tạo nguyên tử", "QUIZ", 1, 50);
                seedNodes(island1, "Hạt nhân và Electron", "QUIZ", 2, 50);
                seedNodes(island1, "Bảng tuần hoàn", "QUIZ", 3, 50);
                seedNodes(island1, "Vệ binh Proton", "BOSS", 4, 200);

                // Island 2
                MapIsland island2 = new MapIsland();
                island2.setName("Vương quốc Liên kết");
                island2.setDescription("Khám phá cách các nguyên tử kết nối với nhau.");
                island2.setOrderIndex(2);
                island2.setUnlockLevel(5);
                island2 = mapIslandRepository.save(island2);

                seedNodes(island2, "Liên kết ion", "QUIZ", 1, 75);
                seedNodes(island2, "Liên kết cộng hóa trị", "QUIZ", 2, 75);
                seedNodes(island2, "Hóa trị và Số oxi hóa", "QUIZ", 3, 75);
                seedNodes(island2, "Chúa tể Electron", "BOSS", 4, 300);

                // Island 3
                MapIsland island3 = new MapIsland();
                island3.setName("Đại dương Axit");
                island3.setDescription("Hành trình chinh phục sức mạnh của dung dịch.");
                island3.setOrderIndex(3);
                island3.setUnlockLevel(10);
                island3 = mapIslandRepository.save(island3);

                seedNodes(island3, "Độ pH và Chỉ thị", "QUIZ", 1, 100);
                seedNodes(island3, "Phản ứng Axit-Bazơ", "QUIZ", 2, 100);
                seedNodes(island3, "Vua Thủy Ngân", "BOSS", 3, 500);
            }
        };
    }

    private void seedNodes(MapIsland island, String name, String type, int order, int xp) {
        MapNode node = new MapNode();
        node.setIsland(island);
        node.setName(name);
        node.setNodeType(type);
        node.setOrderIndex(order);
        node.setXpReward(xp);
        mapNodeRepository.save(node);
    }

    private void seedQuestion(User admin, String prompt, String a, String b, String c, String d, String correct) {
        QuestionBankItem q = new QuestionBankItem();
        q.setCreatedBy(admin);
        q.setPrompt(prompt);
        q.setOptionA(a);
        q.setOptionB(b);
        q.setOptionC(c);
        q.setOptionD(d);
        q.setCorrectOption(correct);
        q.setCreatedAt(java.time.LocalDateTime.now());
        questionBankItemRepository.save(q);
    }
}
