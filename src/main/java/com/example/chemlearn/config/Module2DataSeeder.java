package com.example.chemlearn.config;

import com.example.chemlearn.lab.entity.Lab;
import com.example.chemlearn.lab.entity.LabConfiguration;
import com.example.chemlearn.lab.entity.UserLabProgress;
import com.example.chemlearn.lab.enums.Difficulty;
import com.example.chemlearn.lab.enums.LabCategory;
import com.example.chemlearn.lab.enums.LabType;
import com.example.chemlearn.lab.repository.LabConfigurationRepository;
import com.example.chemlearn.lab.repository.LabRepository;
import com.example.chemlearn.lab.repository.UserLabProgressRepository;
import com.example.chemlearn.lms.entity.*;
import com.example.chemlearn.lms.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.chemlearn.core.entity.Parent;
import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.core.entity.Teacher;
import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.gamification.entity.MapIsland;
import com.example.chemlearn.gamification.entity.MapNode;
import com.example.chemlearn.gamification.entity.MapNodeQuestion;
import com.example.chemlearn.gamification.repository.MapIslandRepository;
import com.example.chemlearn.gamification.repository.MapNodeRepository;
import com.example.chemlearn.gamification.repository.MapNodeQuestionRepository;
import com.example.chemlearn.lms.enums.MaterialScope;
import com.example.chemlearn.lms.enums.QuestionType;

import static com.example.chemlearn.util.PasswordUtil.hash;

import com.example.chemlearn.gamification.entity.Item;
import com.example.chemlearn.gamification.entity.PetSpecies;
import com.example.chemlearn.gamification.entity.EggDropRate;
import com.example.chemlearn.gamification.enums.ItemType;
import com.example.chemlearn.gamification.repository.ItemRepository;
import com.example.chemlearn.gamification.repository.PetSpeciesRepository;
import com.example.chemlearn.gamification.repository.EggDropRateRepository;
import com.example.chemlearn.lab.entity.InventoryItem;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class Module2DataSeeder {

    private final UserRepository accountRepository;
    private final ChapterRepository chapterRepository;
    private final LessonRepository lessonRepository;
    private final MiniQuizQuestionRepository miniQuizQuestionRepository;
    private final TeacherRepository teacherRepository;
    private final StudyClassRepository studyClassRepository;
    private final ClassStudentLinkRepository classStudentLinkRepository;
    private final ParentStudentLinkRepository parentStudentLinkRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final MapIslandRepository mapIslandRepository;
    private final MapNodeRepository mapNodeRepository;
    private final MapNodeQuestionRepository mapNodeQuestionRepository;
    private final QuestionBankItemRepository questionBankItemRepository;
    private final LabRepository labRepository;
    private final LabConfigurationRepository labConfigurationRepository;
    private final UserLabProgressRepository userLabProgressRepository;
    private final StudyClassAssignmentRepository studyClassAssignmentRepository;
    private final ItemRepository itemRepository;
    private final PetSpeciesRepository petSpeciesRepository;
    private final EggDropRateRepository eggDropRateRepository;
    private final com.example.chemlearn.lab.repository.InventoryRepository inventoryRepository;
    private Set<String> pvpQuestionPrompts;

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
            accountRepository.findByUsername("thor")
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


            // ── Student 1: student (Main student) ─────────────────────────────
            User studentUser = accountRepository.findByUsername("student")
                    .orElseGet(() -> {
                        User u = new User();
                        u.setUsername("student");
                        u.setFullName("Học sinh Test");
                        u.setEmail("student@example.com");
                        u.setPassword(hash("123456"));
                        u.setRole(UserRole.ROLE_STUDENT);
                        u.setIsActive(true);
                        u = accountRepository.save(u);

                        Student s = new Student();
                        s.setUsers(u);
                        s.setGradeLevel(8);
                        s.setExperience(0);
                        s.setCurrentStreak(0);
                        s.setCoins(5000);
                        s.setPvpWins(0);
                        s.setSchoolName("Trường THCS Hóa Học");
                        studentRepository.save(s);

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

            // ── LMS Study Zone seed data ──────────────────────────────────────
            User teacherUser = accountRepository.findByUsername("teacher1")
                    .orElseGet(() -> {
                        User u = new User();
                        u.setUsername("teacher1");
                        u.setFullName("Nguyễn Thị Hòa");
                        u.setEmail("teacher1@chemlearn.local");
                        u.setPassword(hash("123456"));
                        u.setRole(UserRole.ROLE_TEACHER);
                        u.setIsActive(true);
                        return accountRepository.save(u);
                    });

            Teacher teacher = teacherRepository.findById(teacherUser.getId())
                    .orElseGet(() -> {
                        Teacher t = new Teacher();
                        t.setUsers(teacherUser);
                        t.setBio("Giáo viên Hóa học phụ trách Study Zone.");
                        t.setSpecialization("Hóa học đại cương");
                        t.setDegree("Cử nhân Sư phạm Hóa học");
                        t.setWorkplace("ChemLearn Academy");
                        return teacherRepository.save(t);
                    });

            if (studyClassRepository.count() == 0) {
                StudyClass class8 = new StudyClass();
                class8.setName("Study Zone 8A");
                class8.setDescription("Lớp ôn tập nền tảng Hóa học cho học sinh lớp 8.");
                class8.setClassCode("8CH101");
                class8.setGradeLevel(8);
                class8.setClassType("STUDY_ZONE");
                class8.setTeacher(teacher);
                StudyClass savedClass8 = studyClassRepository.save(class8);

                StudyClass class9 = new StudyClass();
                class9.setName("Study Zone 9A");
                class9.setDescription("Lớp ôn tập liên kết hóa học và phản ứng cơ bản.");
                class9.setClassCode("9CH102");
                class9.setGradeLevel(9);
                class9.setClassType("STUDY_ZONE");
                class9.setTeacher(teacher);
                StudyClass savedClass9 = studyClassRepository.save(class9);

                StudyClass class10 = new StudyClass();
                class10.setName("Study Zone 10A");
                class10.setDescription("Lớp nâng cao về axit, bazơ và phản ứng trung hòa.");
                class10.setClassCode("10CH03");
                class10.setGradeLevel(10);
                class10.setClassType("STUDY_ZONE");
                class10.setTeacher(teacher);
                StudyClass savedClass10 = studyClassRepository.save(class10);

                Chapter chapter1 = new Chapter();
                chapter1.setTitle("Nguyên tử và nguyên tố");
                chapter1.setDescription("Khái niệm cơ bản về cấu trúc nguyên tử và các nguyên tố.");
                chapter1.setGradeLevel(8);
                chapter1.setOrderIndex(1);
                chapter1.setPublished(true);
                chapter1.setCreatedBy(accountRepository.findByUsername("admin").orElseThrow());
                chapter1.setUpdatedBy(accountRepository.findByUsername("admin").orElseThrow());
                chapter1.setMaterialScope(MaterialScope.GLOBAL);
                chapter1 = chapterRepository.save(chapter1);

                Chapter chapter2 = new Chapter();
                chapter2.setTitle("Liên kết hóa học");
                chapter2.setDescription("Tìm hiểu các kiểu liên kết và cách hình thành phân tử.");
                chapter2.setGradeLevel(9);
                chapter2.setOrderIndex(2);
                chapter2.setPublished(true);
                chapter2.setCreatedBy(accountRepository.findByUsername("admin").orElseThrow());
                chapter2.setUpdatedBy(accountRepository.findByUsername("admin").orElseThrow());
                chapter2.setMaterialScope(MaterialScope.GLOBAL);
                chapter2 = chapterRepository.save(chapter2);

                Chapter chapter3 = new Chapter();
                chapter3.setTitle("Axit, bazơ và muối");
                chapter3.setDescription("Ôn tập tính chất, phản ứng và nhận biết dung dịch.");
                chapter3.setGradeLevel(10);
                chapter3.setOrderIndex(3);
                chapter3.setPublished(true);
                chapter3.setCreatedBy(accountRepository.findByUsername("admin").orElseThrow());
                chapter3.setUpdatedBy(accountRepository.findByUsername("admin").orElseThrow());
                chapter3.setMaterialScope(MaterialScope.GLOBAL);
                chapter3 = chapterRepository.save(chapter3);

                savedClass8.getChapters().add(chapter1);
                savedClass9.getChapters().add(chapter2);
                savedClass10.getChapters().add(chapter3);
                studyClassRepository.save(savedClass8);
                studyClassRepository.save(savedClass9);
                studyClassRepository.save(savedClass10);

                User admin = accountRepository.findByUsername("admin").orElseThrow();

                Lesson lesson1 = new Lesson();
                lesson1.setChapter(chapter1);
                lesson1.setTitle("Cấu trúc nguyên tử");
                lesson1.setContentType("TEXT");
                lesson1.setTextContent(
                        "<h2>Cấu trúc nguyên tử</h2>" +
                                "<p><strong>Tổng quan ngắn:</strong> Nguyên tử gồm hạt nhân (proton và neutron) và lớp electron bao quanh. Số proton xác định số hiệu nguyên tử, còn electron quyết định tính chất hoá học.</p>" +
                                "<figure style=\"max-width:520px;\">" +
                                "<figcaption>Hình: Mô hình Bohr minh họa hạt nhân và các lớp electron (nguồn: Wikimedia Commons).</figcaption>" +
                                "</figure>" +
                                "<h3>Ý tưởng chính</h3>" +
                                "<img src=\"https://cdn.vungoi.vn/vungoi/2022/0806/1659754212619_Thiet_ke_chua_co_ten_(20).png\" alt=\"Sơ đồ React\" />" +
                                "<ul>" +
                                "<li><strong>Proton:</strong> mang điện dương, xác định số hiệu nguyên tử (Z).</li>" +
                                "<li><strong>Neutron:</strong> trung hoà, ảnh hưởng tới khối lượng và đồng vị.</li>" +
                                "<li><strong>Electron:</strong> mang điện âm, nằm ở các lớp vỏ và tham gia liên kết hoá học.</li>" +
                                "</ul>" +
                                "<h3>Ví dụ / ẩn dụ đơn giản</h3>" +
                                "<p>Hãy tưởng tượng nguyên tử như một hệ Mặt Trời thu nhỏ: hạt nhân là 'Mặt Trời' (proton + neutron) và electron là các 'hành tinh' quay xung quanh trên các quỹ đạo khác nhau.</p>" +
                                "<h3>Những nhầm lẫn phổ biến</h3>" +
                                "<ul>" +
                                "<li>Không nhầm lẫn số hiệu nguyên tử (số proton) với khối lượng nguyên tử (proton + neutron).</li>" +
                                "<li>Electron không nằm cố định tại một vị trí; chúng có phân bố xác suất (mô hình orbital) chứ không giống hạt nhỏ quay theo quỹ đạo cổ điển.</li>" +
                                "</ul>" +
                                "<p><strong>Tóm tắt:</strong> Hiểu rõ vai trò của proton, neutron và electron giúp giải thích cấu tạo nguyên tố, đồng vị và hành vi hoá học cơ bản.</p>"
                );
                lesson1.setDurationMinutes(15);
                lesson1.setOrderIndex(1);
                lesson1.setPublished(true);
                lesson1.setCreatedBy(admin);
                lesson1.setUpdatedBy(admin);
                lesson1.setMaterialScope(MaterialScope.GLOBAL);
                lesson1 = lessonRepository.save(lesson1);

                Lesson lesson2 = new Lesson();
                lesson2.setChapter(chapter2);
                lesson2.setTitle("Liên kết ion và cộng hóa trị");
                lesson2.setContentType("TEXT");
                lesson2.setTextContent(
                        "<p>Nguyên tử thường không tồn tại bền vững ở trạng thái riêng lẻ. Chúng có xu hướng liên kết với nhau để đạt cấu hình electron ổn định hơn. Trong bài này, ta phân biệt hai kiểu liên kết cơ bản: liên kết ion và liên kết cộng hóa trị.</p>" +
                                "<h2>1. Liên kết ion</h2>" +
                                "<img src=\"https://vnmedia2.monkeyuni.net/upload/web/img/lien-ket-ion-1.jpg\" alt=\"Sơ đồ React\" />" +
                                "<p>Liên kết ion thường hình thành giữa kim loại và phi kim. Kim loại có xu hướng nhường electron, còn phi kim có xu hướng nhận electron. Khi electron được chuyển từ nguyên tử này sang nguyên tử khác, hai bên trở thành các ion mang điện trái dấu và hút nhau bằng lực tĩnh điện.</p>" +
                                "<p>Ví dụ quen thuộc là <strong>NaCl</strong>. Natri nhường một electron cho clo, tạo ra ion Na<sup>+</sup> và Cl<sup>-</sup>. Hai ion này hút nhau rất mạnh, tạo thành mạng tinh thể ion bền vững.</p>" +
                                "<h2>2. Liên kết cộng hóa trị</h2>" +
                                "<img src=\"https://cdn.luatminhkhue.vn/lmk/article/Screenshot_20221112_022350.png\" alt=\"Sơ đồ React\" />" +
                                "<p>Liên kết cộng hóa trị thường hình thành giữa hai phi kim. Thay vì trao đổi electron, các nguyên tử dùng chung một hoặc nhiều cặp electron để cả hai cùng đạt trạng thái bền hơn.</p>" +
                                "<p>Ví dụ, trong phân tử nước <strong>H<sub>2</sub>O</strong>, nguyên tử oxy dùng chung electron với hai nguyên tử hydro. Nhờ đó, các nguyên tử đều có xu hướng đạt cấu hình electron ổn định hơn.</p>" +
                                "<h2>3. Điểm khác nhau quan trọng</h2>" +
                                "<ul>" +
                                "<li><strong>Liên kết ion:</strong> có sự chuyển giao electron.</li>" +
                                "<li><strong>Liên kết cộng hóa trị:</strong> có sự dùng chung electron.</li>" +
                                "<li><strong>Chất ion:</strong> thường tạo tinh thể, nhiệt độ nóng chảy cao và dẫn điện khi nóng chảy hoặc tan trong nước.</li>" +
                                "<li><strong>Chất cộng hóa trị:</strong> thường tạo phân tử riêng lẻ, đa số không dẫn điện và có tính chất linh hoạt hơn tùy cấu trúc.</li>" +
                                "</ul>" +
                                "<h2>4. Cách ghi nhớ nhanh</h2>" +
                                "<p>Hãy nhớ đơn giản như sau: <strong>ion là cho - nhận electron</strong>, còn <strong>cộng hóa trị là dùng chung electron</strong>. Khi hiểu được nguyên tắc này, bạn sẽ dễ dàng phân biệt cấu tạo và tính chất của rất nhiều chất hóa học trong các bài học tiếp theo.</p>"
                );
                lesson2.setDurationMinutes(18);
                lesson2.setOrderIndex(1);
                lesson2.setPublished(true);
                lesson2.setCreatedBy(admin);
                lesson2.setUpdatedBy(admin);
                lesson2.setMaterialScope(MaterialScope.GLOBAL);
                lesson2 = lessonRepository.save(lesson2);

                Lesson lesson3 = new Lesson();
                lesson3.setChapter(chapter3);
                lesson3.setTitle("Nhận biết axit và bazơ");
                lesson3.setContentType("TEXT");
                lesson3.setTextContent(
                        "<h2>Nhận biết axit và bazơ</h2>" +
                                "<p><strong>Tổng quan ngắn:</strong> Axit và bazơ là hai loại dung dịch có tính chất đối nghịch: axit cho proton (H+), bazơ nhận proton hoặc cho electron. pH và chất chỉ thị (như quỳ tím) là công cụ phổ biến để phân biệt chúng.</p>" +
                                "<figure style=\"max-width:520px;\">" +
                                "<img src=\"https://vietjack.com/cong-thuc/images/thang-ph-cua-dung-dich-cho-biet-dieu-gi.PNG\" alt=\"Thang pH minh họa\" style=\"width:100%;height:auto;\"/>" +
                                "<figcaption>Hình: Thang pH (nguồn: Wikimedia Commons).</figcaption>" +
                                "</figure>" +
                                "<h3>Ý tưởng chính</h3>" +
                                "<ul>" +
                                "<li><strong>pH:</strong> thang đo nồng độ ion H+, pH &lt; 7: axit, pH = 7: trung tính, pH &gt; 7: bazơ.</li>" +
                                "<li><strong>Chỉ thị quỳ tím:</strong> chuyển sang đỏ trong axit, sang xanh trong bazơ.</li>" +
                                "<li><strong>Tính dẫn điện:</strong> dung dịch ion (một số axit/bazơ mạnh) dẫn điện tốt.</li>" +
                                "</ul>" +
                                "<h3>Ví dụ thực tế</h3>" +
                                "<ul>" +
                                "<li>Nước chanh: axit (pH ≈ 2–3).</li>" +
                                "<li>Nước xà phòng loãng: bazơ nhẹ (pH &gt; 7).</li>" +
                                "</ul>" +
                                "<h3>Những nhầm lẫn thường gặp</h3>" +
                                "<ul>" +
                                "<li>Không phải tất cả axit đều ăn mòn ngay lập tức; cường độ axit (mạnh/yếu) khác với nồng độ.</li>" +
                                "<li>Quỳ tím cho chỉ thị tổng quát nhưng không cho giá trị pH chính xác; dùng giấy quỳ pH hoặc máy đo để biết pH số.</li>" +
                                "</ul>" +
                                "<p><strong>Tóm tắt:</strong> Sử dụng pH và chỉ thị để phân biệt axit/bazơ, và nhớ phân biệt cường độ axit (mạnh/yếu) với nồng độ dung dịch.</p>"
                );
                lesson3.setDurationMinutes(20);
                lesson3.setOrderIndex(1);
                lesson3.setPublished(true);
                lesson3.setCreatedBy(admin);
                lesson3.setUpdatedBy(admin);
                lesson3.setMaterialScope(MaterialScope.GLOBAL);
                lesson3 = lessonRepository.save(lesson3);

                if (classStudentLinkRepository.findByStudentIdAndClassRoomId(student2User.getId(), savedClass8.getId()).isEmpty()) {
                    ClassStudentLink link = new ClassStudentLink();
                    link.setClassRoom(savedClass8);
                    link.setStudent(student2User);
                    classStudentLinkRepository.save(link);
                }

                if (classStudentLinkRepository.findByStudentIdAndClassRoomId(student3User.getId(), savedClass9.getId()).isEmpty()) {
                    ClassStudentLink link = new ClassStudentLink();
                    link.setClassRoom(savedClass9);
                    link.setStudent(student3User);
                    classStudentLinkRepository.save(link);
                }

                accountRepository.findByUsername("student").ifPresent(existingStudent -> {
                    if (classStudentLinkRepository.findByStudentIdAndClassRoomId(existingStudent.getId(), savedClass10.getId()).isEmpty()) {
                        ClassStudentLink link = new ClassStudentLink();
                        link.setClassRoom(savedClass10);
                        link.setStudent(existingStudent);
                        classStudentLinkRepository.save(link);
                    }
                });

                seedMiniQuizQuestion(lesson1, admin,
                        "Electron nằm ở đâu trong nguyên tử?", "Trong hạt nhân", "Quanh hạt nhân", "Trong proton", "Trong neutron", "B",
                        "Electron chuyển động xung quanh hạt nhân.");
                seedMiniQuizQuestion(lesson1, admin,
                        "Số hiệu nguyên tử cho biết điều gì?", "Số proton", "Số neutron", "Số electron lớp ngoài", "Khối lượng nguyên tử", "A",
                        "Số hiệu nguyên tử bằng số proton trong hạt nhân.");

                seedMiniQuizQuestion(lesson2, admin,
                        "Liên kết ion thường hình thành khi nào?", "Hai phi kim dùng chung electron", "Kim loại và phi kim trao đổi electron", "Hai kim loại dùng chung electron", "Hai khí hiếm phản ứng với nhau", "B",
                        "Liên kết ion hình thành khi có sự cho nhận electron.");
                seedMiniQuizQuestion(lesson2, admin,
                        "Trong liên kết cộng hóa trị, các nguyên tử thường làm gì?", "Trao đổi proton", "Trao đổi neutron", "Dùng chung electron", "Tạo muối ngay lập tức", "C",
                        "Liên kết cộng hóa trị dùng chung cặp electron.");

                seedMiniQuizQuestion(lesson3, admin,
                        "Dung dịch axit thường có pH như thế nào?", "Lớn hơn 7", "Bằng 7", "Nhỏ hơn 7", "Luôn bằng 14", "C",
                        "Dung dịch axit có pH nhỏ hơn 7.");
                seedMiniQuizQuestion(lesson3, admin,
                        "Quỳ tím chuyển sang màu gì trong môi trường bazơ?", "Đỏ", "Xanh", "Vàng", "Trắng", "B",
                        "Quỳ tím chuyển sang xanh trong môi trường bazơ.");
            }

            // ── PVP Questions ──────────────────────────────────────────────────
            User admin = accountRepository.findByUsername("admin").orElse(null);
            if (admin != null) {
                seedPvpQuestions(admin);
            }

            // ── Game Map: Elemental Archipelago ───────────────────────────────
            if (mapIslandRepository.count() == 0) {
                // Island 1
                MapIsland island1 = new MapIsland();
                island1.setName("Quần đảo Nhập môn");
                island1.setDescription("Nơi bắt đầu hành trình khám phá thế giới nguyên tử.");
                island1.setOrderIndex(1);
                island1.setUnlockLevel(1);
                island1.setImageUrl("https://pub-5a9809d702bf4c298cbf8bbf16bd5374.r2.dev/StarterIsland.png");
                island1 = mapIslandRepository.save(island1);

                seedNodes(island1, "Cấu tạo nguyên tử", "QUIZ", 1, 1000);
                seedNodes(island1, "Hạt nhân và Electron", "QUIZ", 2, 1000);
                seedNodes(island1, "Bảng tuần hoàn", "QUIZ", 3, 1000);
                seedNodes(island1, "Vệ binh Proton", "BOSS", 4, 1000);

                // Island 2
                MapIsland island2 = new MapIsland();
                island2.setName("Vương quốc Liên kết");
                island2.setDescription("Khám phá cách các nguyên tử kết nối với nhau.");
                island2.setOrderIndex(2);
                island2.setUnlockLevel(5);
                island2.setImageUrl("https://pub-5a9809d702bf4c298cbf8bbf16bd5374.r2.dev/BondingKingdom.png");
                island2 = mapIslandRepository.save(island2);

                seedNodes(island2, "Liên kết ion", "QUIZ", 1, 1000);
                seedNodes(island2, "Liên kết cộng hóa trị", "QUIZ", 2, 1000);
                seedNodes(island2, "Hóa trị và Số oxi hóa", "QUIZ", 3, 1000);
                seedNodes(island2, "Chúa tể Electron", "BOSS", 4, 1000);

                // Island 3
                MapIsland island3 = new MapIsland();
                island3.setName("Đại dương Axit");
                island3.setDescription("Hành trình chinh phục sức mạnh của dung dịch.");
                island3.setOrderIndex(3);
                island3.setUnlockLevel(9);
                island3.setImageUrl("https://pub-5a9809d702bf4c298cbf8bbf16bd5374.r2.dev/AxitSea.png");
                island3 = mapIslandRepository.save(island3);

                seedNodes(island3, "Độ pH và Chỉ thị", "QUIZ", 1, 1000);
                seedNodes(island3, "Phản ứng Axit-Bazơ", "QUIZ", 2, 1000);
                seedNodes(island3, "Vua Thủy Ngân", "BOSS", 3, 1000);
            }

            // ── Virtual Labs & Assignments ─────────────────────────────────────
            User adminUser = accountRepository.findByUsername("admin").orElseThrow();
            User teacherUserToSeed = accountRepository.findByUsername("teacher1").orElseThrow();
            Student peterStudent = studentRepository.findById(student3User.getId()).orElseThrow();

            StudyClass targetClassForAssignment = studyClassRepository.findByClassCode("9CH102")
                    .orElseGet(() -> {
                        List<StudyClass> allClasses = studyClassRepository.findAll();
                        return allClasses.isEmpty() ? null : allClasses.get(0);
                    });

            seedVirtualLabs(adminUser, teacherUserToSeed, peterStudent, student3User, targetClassForAssignment);

            // ── Gamification Pet System ───────────────────────────────────────
            if (petSpeciesRepository.count() == 0) {
                // 1. Create items (Egg & Food)
                Item eggItem = new Item();
                eggItem.setName("Trứng Linh Thú Tập Sự");
                eggItem.setDescription("Bao bọc bởi vầng hào quang kỳ bí. Mở ra để nhận 1 Thú Cưng ngẫu nhiên.");
                eggItem.setItemType(ItemType.EGG);
                eggItem.setPriceCoins(500);
                eggItem.setImageUrl("https://pub-5a9809d702bf4c298cbf8bbf16bd5374.r2.dev/egg.png");
                eggItem = itemRepository.save(eggItem);

                Item foodItem = new Item();
                foodItem.setName("Bánh Táo Hóa Học");
                foodItem.setDescription("Món ăn yêu thích của mọi loại Thú Cưng. Cung cấp 500 EXP.");
                foodItem.setItemType(ItemType.FOOD);
                foodItem.setPriceCoins(50);
                foodItem.setEffectValue(500);
                foodItem.setImageUrl("https://pub-5a9809d702bf4c298cbf8bbf16bd5374.r2.dev/Gemini_Generated_Image_i1poaji1poaji1po-removebg-preview.png");
                foodItem = itemRepository.save(foodItem);

                // 2. Create Pet Species
                PetSpecies pet1 = new PetSpecies();
                pet1.setName("Skibidi Tolem");
                pet1.setElement("WATER");
                pet1.setRarity("COMMON");
                pet1.setBaseHp(500);
                pet1.setBaseDamage(50);
                pet1.setHpGrowth(50);
                pet1.setDamageGrowth(5);
                pet1.setSkillName("Phun Nước");
                pet1.setSkillDescription("Gây sát thương hệ Thủy");
                pet1.setImageUrl("https://pub-5a9809d702bf4c298cbf8bbf16bd5374.r2.dev/SkibidiToilem.png");
                pet1 = petSpeciesRepository.save(pet1);

                PetSpecies pet2 = new PetSpecies();
                pet2.setName("Capybara Wizard");
                pet2.setElement("MAGIC");
                pet2.setRarity("RARE");
                pet2.setBaseHp(800);
                pet2.setBaseDamage(90);
                pet2.setHpGrowth(80);
                pet2.setDamageGrowth(9);
                pet2.setSkillName("Phép Thuật Bình Tĩnh");
                pet2.setSkillDescription("Giảm sát thương nhận vào 20%");
                pet2.setImageUrl("https://pub-5a9809d702bf4c298cbf8bbf16bd5374.r2.dev/CapybaraWizard.png");
                pet2 = petSpeciesRepository.save(pet2);

                PetSpecies pet3 = new PetSpecies();
                pet3.setName("Doge Wizard");
                pet3.setElement("LIGHT");
                pet3.setRarity("EPIC");
                pet3.setBaseHp(1200);
                pet3.setBaseDamage(150);
                pet3.setHpGrowth(120);
                pet3.setDamageGrowth(15);
                pet3.setSkillName("Ánh Sáng Doge");
                pet3.setSkillDescription("Hồi phục 10% HP mỗi lượt");
                pet3.setImageUrl("https://pub-5a9809d702bf4c298cbf8bbf16bd5374.r2.dev/DogeWizard.png");
                pet3 = petSpeciesRepository.save(pet3);

                PetSpecies pet4 = new PetSpecies();
                pet4.setName("Tung Sahur Warrior");
                pet4.setElement("EARTH");
                pet4.setRarity("LEGENDARY");
                pet4.setBaseHp(2500);
                pet4.setBaseDamage(300);
                pet4.setHpGrowth(250);
                pet4.setDamageGrowth(30);
                pet4.setSkillName("Địa Chấn Tối Thượng");
                pet4.setSkillDescription("Gây sát thương khủng khiếp lên mọi kẻ địch");
                pet4.setImageUrl("https://pub-5a9809d702bf4c298cbf8bbf16bd5374.r2.dev/TungSahurWarrior.png");
                pet4 = petSpeciesRepository.save(pet4);

                // 3. Create Egg Drop Rates
                seedEggDropRate(eggItem, pet1, 60);
                seedEggDropRate(eggItem, pet2, 25);
                seedEggDropRate(eggItem, pet3, 10);
                seedEggDropRate(eggItem, pet4, 5);
            }

            // Seed monster details and chemistry questions for map nodes
            seedMonsterQuestionsAndDetails();

            // Seed 22 Inventory Items for Virtual Lab
            seedInventoryItems();
            
            // Update existing Inventory Items with explicit HEX iconFill colors
            updateInventoryIconFills();

            // Seed missing special acids if they don't exist
            inventoryRepository.findByItemCode("hcl_dac").ifPresentOrElse(
                existing -> {
                    existing.setIconColor("text-stone-200");
                    existing.setIconFill("#f1f5f9");
                    inventoryRepository.save(existing);
                },
                () -> {
                    InventoryItem hclDac = new InventoryItem();
                    hclDac.setItemCode("hcl_dac");
                    hclDac.setName("Axit HCl (Đặc)");
                    hclDac.setType(com.example.chemlearn.lab.enums.ItemType.CHEMICAL);
                    hclDac.setState(com.example.chemlearn.lab.enums.PhysicalState.LIQUID);
                    hclDac.setSubCategory(com.example.chemlearn.lab.enums.SubCategory.ACID);
                    hclDac.setIconName("Droplet");
                    hclDac.setIconColor("text-stone-200");
                    hclDac.setIconFill("#f1f5f9");
                    inventoryRepository.save(hclDac);
                }
            );

            inventoryRepository.findByItemCode("h2so4_dac").ifPresentOrElse(
                existing -> {
                    existing.setIconColor("text-stone-200");
                    existing.setIconFill("#f1f5f9");
                    inventoryRepository.save(existing);
                },
                () -> {
                    InventoryItem h2so4Dac = new InventoryItem();
                    h2so4Dac.setItemCode("h2so4_dac");
                    h2so4Dac.setName("Axit H2SO4 (Đặc)");
                    h2so4Dac.setType(com.example.chemlearn.lab.enums.ItemType.CHEMICAL);
                    h2so4Dac.setState(com.example.chemlearn.lab.enums.PhysicalState.LIQUID);
                    h2so4Dac.setSubCategory(com.example.chemlearn.lab.enums.SubCategory.ACID);
                    h2so4Dac.setIconName("Droplet");
                    h2so4Dac.setIconColor("text-stone-200");
                    h2so4Dac.setIconFill("#f1f5f9");
                    inventoryRepository.save(h2so4Dac);
                }
            );
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

    private void seedQuestionIfMissing(User admin, String prompt, String a, String b, String c, String d, String correct) {
        if (pvpQuestionPrompts == null) {
            pvpQuestionPrompts = questionBankItemRepository.findAll().stream()
                    .map(question -> question.getPrompt().toLowerCase())
                    .collect(Collectors.toSet());
        }

        if (pvpQuestionPrompts.add(prompt.toLowerCase())) {
            seedQuestion(admin, prompt, a, b, c, d, correct);
        }
    }

    private void seedPvpQuestions(User admin) {
        seedQuestionIfMissing(admin, "H2O là công thức hóa học của chất nào?", "Nước", "Oxy", "Hydro", "Muối ăn", "A");
        seedQuestionIfMissing(admin, "Nguyên tố nào có ký hiệu là Na?", "Natri", "Nitơ", "Kẽm", "Đồng", "A");
        seedQuestionIfMissing(admin, "Axit sunfuric có công thức là gì?", "H2SO4", "HCl", "HNO3", "H2CO3", "A");
        seedQuestionIfMissing(admin, "Kim loại nào ở trạng thái lỏng ở nhiệt độ phòng?", "Thủy ngân", "Sắt", "Chì", "Nhôm", "A");
        seedQuestionIfMissing(admin, "Khí nào duy trì sự cháy?", "Khí Oxy", "Khí Cacbonic", "Khí Nitơ", "Khí Hidro", "A");
        seedQuestionIfMissing(admin, "Công thức hóa học của muối ăn là gì?", "NaCl", "KCl", "CaCO3", "NaOH", "A");
        seedQuestionIfMissing(admin, "Nguyên tố nào có ký hiệu là O?", "Oxi", "Vàng", "Osmium", "Thiếc", "A");
        seedQuestionIfMissing(admin, "CO2 là công thức của chất nào?", "Khí cacbonic", "Khí cacbon monoxit", "Khí clo", "Khí amoniac", "A");
        seedQuestionIfMissing(admin, "Dung dịch axit thường có pH như thế nào?", "Nhỏ hơn 7", "Bằng 7", "Lớn hơn 7", "Luôn bằng 14", "A");
        seedQuestionIfMissing(admin, "Dung dịch bazơ thường có pH như thế nào?", "Lớn hơn 7", "Bằng 7", "Nhỏ hơn 7", "Luôn bằng 0", "A");
        seedQuestionIfMissing(admin, "Quỳ tím chuyển màu gì trong môi trường axit?", "Đỏ", "Xanh", "Vàng", "Không màu", "A");
        seedQuestionIfMissing(admin, "Quỳ tím chuyển màu gì trong môi trường bazơ?", "Xanh", "Đỏ", "Tím nhạt", "Không màu", "A");
        seedQuestionIfMissing(admin, "Phản ứng giữa axit và bazơ thường tạo ra gì?", "Muối và nước", "Kim loại và oxi", "Axit mới và khí hidro", "Bazơ mới và khí clo", "A");
        seedQuestionIfMissing(admin, "HCl là tên viết tắt của axit nào?", "Axit clohiđric", "Axit sunfuric", "Axit nitric", "Axit axetic", "A");
        seedQuestionIfMissing(admin, "NaOH thuộc loại chất nào?", "Bazơ", "Axit", "Muối", "Oxit axit", "A");
        seedQuestionIfMissing(admin, "CaCO3 thường được gọi là gì?", "Canxi cacbonat", "Canxi clorua", "Canxi oxit", "Canxi hidroxit", "A");
        seedQuestionIfMissing(admin, "Nguyên tử được cấu tạo từ hạt nhân và loại hạt nào chuyển động xung quanh?", "Electron", "Proton", "Neutron", "Ion dương", "A");
        seedQuestionIfMissing(admin, "Hạt nào trong nguyên tử mang điện tích dương?", "Proton", "Electron", "Neutron", "Phân tử", "A");
        seedQuestionIfMissing(admin, "Hạt nào trong nguyên tử mang điện tích âm?", "Electron", "Proton", "Neutron", "Nguyên tử khối", "A");
        seedQuestionIfMissing(admin, "Hạt nào trong nguyên tử không mang điện?", "Neutron", "Electron", "Proton", "Ion", "A");
        seedQuestionIfMissing(admin, "Liên kết ion thường hình thành giữa nhóm nguyên tố nào?", "Kim loại và phi kim", "Hai phi kim", "Hai khí hiếm", "Hai kim loại kiềm", "A");
        seedQuestionIfMissing(admin, "Liên kết cộng hóa trị hình thành do các nguyên tử làm gì?", "Dùng chung electron", "Trao đổi proton", "Mất hết neutron", "Tạo electron mới", "A");
        seedQuestionIfMissing(admin, "Oxit nào sau đây là oxit bazơ?", "CaO", "CO2", "SO2", "P2O5", "A");
        seedQuestionIfMissing(admin, "Oxit nào sau đây là oxit axit?", "CO2", "Na2O", "CaO", "MgO", "A");
        seedQuestionIfMissing(admin, "Khi đốt cháy than trong oxi, sản phẩm chính thường là gì?", "CO2", "H2", "NaCl", "NH3", "A");
        seedQuestionIfMissing(admin, "Khí hidro có công thức hóa học là gì?", "H2", "O2", "N2", "Cl2", "A");
        seedQuestionIfMissing(admin, "Khí nitơ có công thức hóa học là gì?", "N2", "Na", "NO2", "NH3", "A");
        seedQuestionIfMissing(admin, "Nước vôi trong là dung dịch của chất nào?", "Ca(OH)2", "NaOH", "HCl", "CaCO3", "A");
        seedQuestionIfMissing(admin, "CO2 làm nước vôi trong xuất hiện hiện tượng gì?", "Vẩn đục trắng", "Chuyển xanh", "Phát sáng", "Tạo kim loại đồng", "A");
        seedQuestionIfMissing(admin, "Kim loại kẽm tác dụng với HCl sinh ra khí nào?", "H2", "O2", "CO2", "Cl2", "A");
        seedQuestionIfMissing(admin, "Sắt có ký hiệu hóa học là gì?", "Fe", "S", "Si", "Ag", "A");
        seedQuestionIfMissing(admin, "Đồng có ký hiệu hóa học là gì?", "Cu", "Co", "Cl", "Ca", "A");
        seedQuestionIfMissing(admin, "Bạc có ký hiệu hóa học là gì?", "Ag", "Au", "Al", "Ar", "A");
        seedQuestionIfMissing(admin, "Vàng có ký hiệu hóa học là gì?", "Au", "Ag", "Al", "O", "A");
        seedQuestionIfMissing(admin, "Nhôm có ký hiệu hóa học là gì?", "Al", "Ag", "Au", "Am", "A");
        seedQuestionIfMissing(admin, "Clo có ký hiệu hóa học là gì?", "Cl", "C", "Ca", "Co", "A");
        seedQuestionIfMissing(admin, "Magiê có ký hiệu hóa học là gì?", "Mg", "Mn", "Mo", "Hg", "A");
        seedQuestionIfMissing(admin, "Số mol được tính bằng công thức nào nếu biết khối lượng và khối lượng mol?", "n = m / M", "n = M / m", "n = m x M", "n = V x M", "A");
        seedQuestionIfMissing(admin, "Ở điều kiện tiêu chuẩn, 1 mol khí chiếm thể tích xấp xỉ bao nhiêu?", "22,4 lít", "2,24 lít", "24 gam", "6,02 lít", "A");
        seedQuestionIfMissing(admin, "Số Avogadro xấp xỉ bằng bao nhiêu?", "6,02 x 10^23", "3,14 x 10^8", "9,81", "1,66 x 10^-24", "A");
        seedQuestionIfMissing(admin, "Chất nào sau đây là muối?", "NaCl", "HCl", "NaOH", "H2O", "A");
        seedQuestionIfMissing(admin, "Chất nào sau đây là axit?", "HNO3", "NaCl", "KOH", "CaO", "A");
        seedQuestionIfMissing(admin, "Chất nào sau đây là bazơ?", "KOH", "CO2", "H2SO4", "NaCl", "A");
        seedQuestionIfMissing(admin, "Fe2O3 là oxit của kim loại nào?", "Sắt", "Đồng", "Nhôm", "Kẽm", "A");
        seedQuestionIfMissing(admin, "CuSO4 thường có màu gì khi ở dạng dung dịch?", "Xanh lam", "Đỏ tươi", "Không màu hoàn toàn", "Đen", "A");
    }

    private void seedMiniQuizQuestion(Lesson lesson,
                                      User createdBy,
                                      String prompt,
                                      String optionA,
                                      String optionB,
                                      String optionC,
                                      String optionD,
                                      String correctOption,
                                      String explanation) {
        MiniQuizQuestion question = new MiniQuizQuestion();
        question.setLesson(lesson);
        question.setCreatedBy(createdBy);
        question.setPrompt(prompt);
        question.setQuestionType(QuestionType.SINGLE_CHOICE);
        question.setOptionA(optionA);
        question.setOptionB(optionB);
        question.setOptionC(optionC);
        question.setOptionD(optionD);
        question.setCorrectOption(correctOption);
        question.setExplanation(explanation);
        miniQuizQuestionRepository.save(question);
    }

    private void seedVirtualLabs(User admin, User teacher, Student student, User studentUser, StudyClass targetClass) {
        if (labConfigurationRepository.count() > 0) {
            return; // Already seeded configurations
        }

        // Clear existing labs to prevent duplicates if any existed without config
        if (labRepository.count() > 0) {
            userLabProgressRepository.deleteAll();
            labConfigurationRepository.deleteAll();
            studyClassAssignmentRepository.deleteAll();
            labRepository.deleteAll();
        }

        // 1. [PREMADE] Điều chế Hidro (Lớp 8)
        Lab lab1 = new Lab();
        lab1.setTitle("Điều chế khí Hidro");
        lab1.setDescription("Thực hành phản ứng giữa kim loại Kẽm (Zn) và Axit Clohidric (HCl) để sinh ra khí Hidro.");
        lab1.setCategory(LabCategory.KIM_LOAI);
        lab1.setDifficulty(Difficulty.EASY);
        lab1.setType(LabType.PREMADE);
        lab1.setAuthorId(admin.getId());
        lab1.setMaxScore(0);
        lab1 = labRepository.save(lab1);
        seedLabConfiguration(lab1, 
                Map.of("allowed_chemicals", List.of("beaker", "test_tube", "bunsen_burner", "zn_grain", "hcl")), 
                Map.of("offset", Map.of("x", 0, "y", 0), "zoom_scale", 1.0));

        // 2. [PREMADE] Tính chất hoá học của nước (Lớp 8)
        Lab lab2 = new Lab();
        lab2.setTitle("Tính chất hoá học của Nước");
        lab2.setDescription("Khảo sát phản ứng mãnh liệt của Natri (Na) với nước, sau đó dùng Phenolphtalein để kiểm chứng dung dịch sinh ra có tính kiềm.");
        lab2.setCategory(LabCategory.GENERAL);
        lab2.setDifficulty(Difficulty.MEDIUM);
        lab2.setType(LabType.PREMADE);
        lab2.setAuthorId(admin.getId());
        lab2.setMaxScore(0);
        lab2 = labRepository.save(lab2);
        seedLabConfiguration(lab2, 
                Map.of("allowed_chemicals", List.of("beaker", "test_tube", "bunsen_burner", "sodium", "water", "phenolphthalein")), 
                Map.of("offset", Map.of("x", 0, "y", 0), "zoom_scale", 1.0));

        // 3. [PREMADE] Phân loại chất bằng quỳ tím (Lớp 9)
        Lab lab3 = new Lab();
        lab3.setTitle("Phân loại chất bằng chất chỉ thị");
        lab3.setDescription("Dùng Quỳ tím và Phenolphtalein để nhận biết môi trường Axit (HCl) và Bazơ (NaOH).");
        lab3.setCategory(LabCategory.AXIT_BAZO);
        lab3.setDifficulty(Difficulty.EASY);
        lab3.setType(LabType.PREMADE);
        lab3.setAuthorId(admin.getId());
        lab3.setMaxScore(0);
        lab3 = labRepository.save(lab3);
        seedLabConfiguration(lab3, 
                Map.of("allowed_chemicals", List.of("beaker", "test_tube", "bunsen_burner", "hcl", "naoh_sol", "phenolphthalein", "litmus_paper")), 
                Map.of("offset", Map.of("x", 0, "y", 0), "zoom_scale", 1.0));

        // 4. [PREMADE] Phản ứng trao đổi trong dung dịch (Lớp 9)
        Lab lab4 = new Lab();
        lab4.setTitle("Phản ứng trao đổi trong dung dịch");
        lab4.setDescription("Quan sát phản ứng trao đổi tạo kết tủa trắng đặc trưng. Thử nghiệm với các cặp muối: AgNO3 - NaCl và BaCl2 - Na2SO4.");
        lab4.setCategory(LabCategory.KET_TUA);
        lab4.setDifficulty(Difficulty.MEDIUM);
        lab4.setType(LabType.PREMADE);
        lab4.setAuthorId(admin.getId());
        lab4.setMaxScore(0);
        lab4 = labRepository.save(lab4);
        seedLabConfiguration(lab4, 
                Map.of("allowed_chemicals", List.of("beaker", "test_tube", "bunsen_burner", "bacl2", "na2so4", "agno3", "nacl")), 
                Map.of("offset", Map.of("x", 0, "y", 0), "zoom_scale", 1.0));

        // 5. [ASSIGNMENT] Kiểm tra Thực hành: Phân biệt dung dịch
        Lab lab5 = new Lab();
        lab5.setTitle("Kiểm tra Thực hành: Phân biệt dung dịch");
        lab5.setDescription("Bằng phương pháp hóa học, hãy phân biệt 3 lọ dung dịch không dán nhãn chứa: HCl, NaOH và NaCl. Kéo thả các lọ hóa chất và dụng cụ ra bàn làm việc, thực hiện phản ứng và sắp xếp chúng theo đúng thứ tự.");
        lab5.setCategory(LabCategory.AXIT_BAZO);
        lab5.setDifficulty(Difficulty.HARD);
        lab5.setType(LabType.ASSIGNMENT);
        lab5.setAuthorId(teacher.getId());
        lab5.setMaxScore(100);
        lab5 = labRepository.save(lab5);
        seedLabConfiguration(lab5,
                Map.of("allowHints", false, "durationMinutes", 15, "showReactionToast", false),
                Map.of("offset", Map.of("x", 0, "y", 0), "zoom_scale", 1.0)
        );

        // Assign to Class (Chỉ thực hiện nếu có class)
        if (targetClass != null) {
            StudyClassAssignment assignment = new StudyClassAssignment();
            assignment.setStudyClassField(targetClass);
            assignment.setTitle("Kiểm tra Thực hành: Phân biệt dung dịch (15 phút)");
            assignment.setLab(lab5);
            assignment.setDueDate(Instant.now().plus(7, ChronoUnit.DAYS));
            studyClassAssignmentRepository.save(assignment);
        }

        // 6. [SANDBOX] Bàn thực hành tự do của tôi
        Lab lab6 = new Lab();
        lab6.setTitle("Phòng thí nghiệm tự do của " + studentUser.getFullName());
        lab6.setDescription("Phòng thí nghiệm tự do của bạn. Hãy thoả sức sáng tạo.");
        lab6.setCategory(LabCategory.GENERAL);
        lab6.setType(LabType.SANDBOX);
        lab6.setAuthorId(student.getUsers().getId());
        lab6.setMaxScore(0);
        lab6 = labRepository.save(lab6);
        seedLabConfiguration(lab6, new HashMap<>(), Map.of("offset", Map.of("x", 0, "y", 0), "zoom_scale", 1.0));

        // 6. Seed UserLabProgress cho học sinh
        // Progress cho bài lab 1 (Đang làm)
        UserLabProgress p1 = new UserLabProgress();
        p1.setStudent(student);
        p1.setLab(lab1);
        p1.setStatus("IN_PROGRESS");
        p1.setProgressPercent(33);
        p1.setCurrentScore(10);
        p1.setCompletedActions(List.of("DRAG_FLASK_TO_WORKSPACE", "HCl_NaOH"));
        p1.setCurrentWorkspace(List.of(
                Map.of(
                        "content", "NaCl + H₂O",
                        "templateId", "beaker",
                        "liquidContent", "NaCl + H₂O",
                        "liquidColor", "rgba(200, 230, 255, 0.7)",
                        "x", 300.0,
                        "y", 250.0
                )
        ));
        p1.setViewport(Map.of("offset", Map.of("x", 0, "y", 0), "zoom_scale", 1.2));
        p1.setIsFinished(false);
        p1.setStartedAt(Instant.now());
        p1.setLastEditedAt(Instant.now());
        userLabProgressRepository.save(p1);

        // Progress cho bài lab 2 (Đã xong)
        UserLabProgress p2 = new UserLabProgress();
        p2.setStudent(student);
        p2.setLab(lab2);
        p2.setStatus("COMPLETED");
        p2.setProgressPercent(100);
        p2.setCurrentScore(50);
        p2.setCompletedActions(List.of("DRAG_FLASK_TO_WORKSPACE", "Fe_HCl", "HEAT_FLASK"));
        p2.setCurrentWorkspace(List.of(
                Map.of(
                        "content", "FeCl₂ + H₂↑",
                        "templateId", "beaker",
                        "liquidContent", "FeCl₂",
                        "liquidColor", "rgba(187, 247, 208, 0.7)",
                        "isHeated", true,
                        "x", 400.0,
                        "y", 300.0
                )
        ));
        p2.setViewport(Map.of("offset", Map.of("x", 0, "y", 0), "zoom_scale", 1.5));
        p2.setIsFinished(true);
        p2.setSubmittedAt(Instant.now().minus(1, ChronoUnit.DAYS));
        p2.setStartedAt(Instant.now());
        p2.setLastEditedAt(Instant.now());
        userLabProgressRepository.save(p2);
    }

    private void seedLabConfiguration(Lab lab, Map<String, Object> config, Map<String, Object> viewport) {
        LabConfiguration conf = new LabConfiguration();
        conf.setLab(lab);
        conf.setConfig(config);
        conf.setViewport(viewport);
        conf.setInitialWorkspace(new ArrayList<>());
        labConfigurationRepository.save(conf);
    }

    private void seedEggDropRate(Item eggItem, PetSpecies species, int weight) {
        EggDropRate rate = new EggDropRate();
        rate.setEggItem(eggItem);
        rate.setPetSpecies(species);
        rate.setDropWeight(weight);
        eggDropRateRepository.save(rate);
    }

    private void seedMonsterQuestionsAndDetails() {
        // 1. Force update island unlock levels if they exist
        mapIslandRepository.findAll().forEach(island -> {
            if ("Quần đảo Nhập môn".equals(island.getName())) {
                island.setUnlockLevel(1);
                mapIslandRepository.save(island);
            } else if ("Vương quốc Liên kết".equals(island.getName()) || "Vương Quốc Liên Kết".equals(island.getName())) {
                island.setUnlockLevel(5);
                mapIslandRepository.save(island);
            } else if ("Đại dương Axit".equals(island.getName()) || "Biển Axit".equals(island.getName())) {
                island.setUnlockLevel(9);
                mapIslandRepository.save(island);
            }
        });

        // 2. Update existing MapNode details for monsters if missing, and set XP reward to 1000
        List<MapNode> allNodes = mapNodeRepository.findAll();
        for (MapNode node : allNodes) {
            node.setXpReward(1000);
            boolean isBoss = "BOSS".equals(node.getNodeType());
            if (node.getMonsterImageUrl() == null || node.getMonsterImageUrl().isEmpty()) {
                if (!isBoss) {
                    node.setMonsterName("Green Slime");
                    node.setMonsterImageUrl("https://pub-5a9809d702bf4c298cbf8bbf16bd5374.r2.dev/GreenSlime.png");
                    node.setMonsterIdleUrl("https://pub-5a9809d702bf4c298cbf8bbf16bd5374.r2.dev/GreenSlime.png");
                } else {
                    if ("Vệ binh Proton".equals(node.getName())) {
                        node.setMonsterName("Vệ binh Proton");
                        node.setMonsterImageUrl("https://pub-5a9809d702bf4c298cbf8bbf16bd5374.r2.dev/ProtonGuardian.png");
                        node.setMonsterIdleUrl("https://pub-5a9809d702bf4c298cbf8bbf16bd5374.r2.dev/ProtonGuardian.png");
                    } else if ("Chúa tể Electron".equals(node.getName())) {
                        node.setMonsterName("Chúa tể Electron");
                        node.setMonsterImageUrl("https://pub-5a9809d702bf4c298cbf8bbf16bd5374.r2.dev/ElectronOverlord.png");
                        node.setMonsterIdleUrl("https://pub-5a9809d702bf4c298cbf8bbf16bd5374.r2.dev/ElectronOverlord.png");
                    } else if ("Vua Thủy Ngân".equals(node.getName())) {
                        node.setMonsterName("Vua Thủy Ngân");
                        node.setMonsterImageUrl("https://pub-5a9809d702bf4c298cbf8bbf16bd5374.r2.dev/MercuryKing.png");
                        node.setMonsterIdleUrl("https://pub-5a9809d702bf4c298cbf8bbf16bd5374.r2.dev/MercuryKing.png");
                    }
                }
            }
            mapNodeRepository.save(node);
        }

        // 2. Seed questions for nodes if none exist
        if (mapNodeQuestionRepository.count() == 0) {
            for (MapNode node : allNodes) {
                String name = node.getName();
                if ("Cấu tạo nguyên tử".equals(name)) {
                    createMapNodeQuestion(node, "Nguyên tử được cấu tạo bởi hạt nhân và vỏ hạt nào?", "Electron", "Proton", "Nơtron", "Ion", "A", "Electron chuyển động xung quanh hạt nhân để tạo thành lớp vỏ nguyên tử.");
                    createMapNodeQuestion(node, "Hạt nào mang điện tích dương trong nguyên tử?", "Proton", "Electron", "Nơtron", "Ion", "A", "Hạt proton nằm trong hạt nhân và mang điện tích dương (+1).");
                    createMapNodeQuestion(node, "Hạt nơtron mang điện tích gì?", "Không mang điện", "Điện tích âm", "Điện tích dương", "Điện tích lưỡng cực", "A", "Nơtron là hạt trung hòa về điện (không mang điện) nằm trong hạt nhân nguyên tử.");
                    createMapNodeQuestion(node, "Hạt nào mang điện tích âm trong nguyên tử?", "Electron", "Proton", "Nơtron", "Hạt nhân", "A", "Electron mang điện tích âm (-1) và di chuyển xung quanh hạt nhân.");
                    createMapNodeQuestion(node, "Nguyên tử trung hòa về điện vì:", "Số proton bằng số electron", "Số proton bằng số nơtron", "Số electron bằng số nơtron", "Khối lượng proton bằng nơtron", "A", "Trong nguyên tử trung hòa về điện, số hạt proton (+) bằng số hạt electron (-).");
                    createMapNodeQuestion(node, "Phần lớn khối lượng của nguyên tử tập trung ở:", "Hạt nhân", "Vỏ electron lớp ngoài", "Khoảng không nguyên tử", "Các hạt electron tự do", "A", "Hạt nhân gồm proton và nơtron có khối lượng lớn hơn electron rất nhiều, tập trung gần như toàn bộ khối lượng nguyên tử.");
                } else if ("Hạt nhân và Electron".equals(name)) {
                    createMapNodeQuestion(node, "Hạt nhân nguyên tử gồm những loại hạt nào?", "Proton và Nơtron", "Proton và Electron", "Electron và Nơtron", "Chỉ có Proton", "A", "Hạt nhân nguyên tử nằm ở tâm, cấu tạo từ hai hạt chính là proton và nơtron.");
                    createMapNodeQuestion(node, "Lớp vỏ electron của nguyên tử được cấu tạo từ loại hạt nào?", "Electron", "Proton", "Nơtron", "Hạt nhân", "A", "Lớp vỏ nguyên tử cấu tạo từ hạt electron sắp xếp theo các phân lớp khác nhau.");
                    createMapNodeQuestion(node, "Số lượng electron tối đa ở lớp thứ nhất (lớp K) là:", "2", "8", "18", "32", "A", "Lớp thứ nhất (gần hạt nhân nhất) chứa tối đa là 2 electron.");
                    createMapNodeQuestion(node, "Số lượng electron tối đa ở lớp thứ hai (lớp L) là:", "8", "2", "18", "10", "A", "Lớp thứ hai chứa tối đa là 8 electron.");
                    createMapNodeQuestion(node, "Kí hiệu của số khối nguyên tử là gì?", "A", "Z", "N", "E", "A", "Số khối kí hiệu là A, bằng tổng số hạt proton (Z) và nơtron (N).");
                    createMapNodeQuestion(node, "Kí hiệu của số hiệu nguyên tử (số proton) là gì?", "Z", "A", "N", "M", "A", "Số hiệu nguyên tử kí hiệu là Z, đại diện cho điện tích hạt nhân và số proton.");
                } else if ("Bảng tuần hoàn".equals(name)) {
                    createMapNodeQuestion(node, "Ai là người đầu tiên công bố bảng tuần hoàn các nguyên tố hóa học?", "Dmitri Mendeleev", "Marie Curie", "Antoine Lavoisier", "Albert Einstein", "A", "Nhà hóa học người Nga Dmitri Mendeleev đã công bố bảng tuần hoàn đầu tiên vào năm 1869.");
                    createMapNodeQuestion(node, "Các nguyên tố trong bảng tuần hoàn được sắp xếp tăng dần theo:", "Điện tích hạt nhân", "Khối lượng nguyên tử", "Bán kính nguyên tử", "Số nơtron", "A", "Nguyên tắc chính là sắp xếp theo chiều tăng dần của điện tích hạt nhân (số hiệu nguyên tử Z).");
                    createMapNodeQuestion(node, "Bảng tuần hoàn hiện đại có bao nhiêu chu kỳ?", "7", "8", "6", "9", "A", "Bảng tuần hoàn hiện tại có 7 chu kỳ đại diện cho 7 lớp electron.");
                    createMapNodeQuestion(node, "Các nguyên tố cùng một nhóm A có đặc điểm chung nào?", "Có cùng số electron hóa trị (lớp ngoài cùng)", "Có cùng số lớp electron", "Có cùng khối lượng nguyên tử", "Có cùng tính kim loại", "A", "Nguyên tố cùng nhóm A có số electron lớp ngoài cùng giống nhau dẫn đến tính chất hóa học tương đồng.");
                    createMapNodeQuestion(node, "Nhóm nguyên tố nào được gọi là nhóm khí hiếm (khí trơ)?", "Nhóm VIIIA", "Nhóm VIIA", "Nhóm IA", "Nhóm IIA", "A", "Nhóm VIIIA chứa các khí hiếm trơ về mặt hóa học do lớp vỏ ngoài cùng bền vững chứa 8 electron (trừ He chứa 2 e).");
                    createMapNodeQuestion(node, "Nguyên tố nào nhẹ nhất trong bảng tuần hoàn?", "Hydro (H)", "Heli (He)", "Liti (Li)", "Cacbon (C)", "A", "Hydro (H) đứng đầu bảng tuần hoàn với khối lượng nguyên tử xấp xỉ bằng 1 u.");
                } else if ("Vệ binh Proton".equals(name)) {
                    createMapNodeQuestion(node, "Nguyên tố có số hiệu nguyên tử Z = 6 là nguyên tố nào?", "Cacbon (C)", "Nitơ (N)", "Oxy (O)", "Flo (F)", "A", "Cacbon có số hiệu nguyên tử bằng 6.");
                    createMapNodeQuestion(node, "Đồng vị là các nguyên tử có cùng số proton nhưng khác nhau về số hạt nào?", "Nơtron", "Electron", "Proton", "Phân tử", "A", "Đồng vị có cùng số hiệu Z (cùng số proton) nhưng khác số khối A do có số nơtron khác nhau.");
                    createMapNodeQuestion(node, "Điện tích quy ước của hạt electron là bao nhiêu?", "-1", "+1", "0", "-2", "A", "Electron mang điện tích âm quy ước là -1.");
                    createMapNodeQuestion(node, "Lực giữ các hạt electron xung quanh hạt nhân là gì?", "Lực hút tĩnh điện giữa điện tích dương (+) hạt nhân và âm (-) electron", "Lực hấp dẫn", "Lực ma sát", "Lực từ trường", "A", "Proton (+) trong hạt nhân và electron (-) hút nhau bằng lực hút tĩnh điện Coulomb.");
                    createMapNodeQuestion(node, "Một nguyên tử có 11 proton và 12 nơtron thì số khối A là bao nhiêu?", "23", "11", "12", "1", "A", "Số khối A = Z + N = 11 + 12 = 23 (đây là nguyên tử Natri).");
                    createMapNodeQuestion(node, "Cấu hình electron của nguyên tử Heli (Z = 2) là gì?", "1s2", "1s1", "2s2", "1s2 2s1", "A", "Heli có 2 electron nằm ở phân lớp 1s, cấu hình là 1s2.");
                    createMapNodeQuestion(node, "Kim loại kiềm thuộc nhóm nào trong bảng tuần hoàn?", "Nhóm IA", "Nhóm IIA", "Nhóm VIIA", "Nhóm VIIIA", "A", "Nhóm IA chứa các kim loại kiềm cực kỳ hoạt động mạnh (trừ Hydro).");
                    createMapNodeQuestion(node, "Halogen là tên gọi chung cho các nguyên tố thuộc nhóm nào?", "Nhóm VIIA", "Nhóm IA", "Nhóm VIIIA", "Nhóm VA", "A", "Nhóm VIIA chứa các phi kim halogen điển hình như Flo, Clo, Brom, Iot.");
                    createMapNodeQuestion(node, "Chu kỳ là dãy các nguyên tố có cùng số lượng gì?", "Lớp electron", "Electron lớp ngoài cùng", "Proton", "Nơtron", "A", "Các nguyên tố trong cùng chu kỳ có cùng số lớp electron chuyển động xung quanh hạt nhân.");
                    createMapNodeQuestion(node, "Nguyên tố có tính phi kim mạnh nhất trong bảng tuần hoàn là:", "Flo (F)", "Clo (Cl)", "Oxy (O)", "Nitơ (N)", "A", "Flo là phi kim có độ âm điện lớn nhất, tính phi kim mạnh nhất bảng tuần hoàn.");
                    createMapNodeQuestion(node, "Đường kính hạt nhân so với đường kính nguyên tử như thế nào?", "Nhỏ hơn khoảng 10.000 lần", "Nhỏ hơn khoảng 2 lần", "Bằng nhau", "Lớn hơn khoảng 100 lần", "A", "Hạt nhân rất nhỏ bé so với nguyên tử, không gian trống bao phủ gần hết thể tích nguyên tử.");
                    createMapNodeQuestion(node, "Nguyên tử khí hiếm nào có lớp electron ngoài cùng đạt cấu hình 1s2?", "Heli (He)", "Neon (Ne)", "Argon (Ar)", "Krypton (Kr)", "A", "He chỉ có 2 electron và đạt cấu hình bền 1s2 ở phân lớp đầu tiên.");
                } else if ("Liên kết ion".equals(name)) {
                    createMapNodeQuestion(node, "Liên kết ion được hình thành bởi lực hút nào?", "Lực hút tĩnh điện giữa hai ion mang điện tích trái dấu", "Sự dùng chung cặp electron", "Lực vạn vật hấp dẫn", "Liên kết cộng hóa trị", "A", "Liên kết ion hình thành do lực hút tĩnh điện giữa cation (+) và anion (-).");
                    createMapNodeQuestion(node, "Cation là loại ion mang điện tích gì?", "Mang điện tích dương", "Mang điện tích âm", "Không mang điện", "Lưỡng tính", "A", "Cation là ion mang điện tích dương (+) sinh ra do nguyên tử kim loại nhường electron.");
                    createMapNodeQuestion(node, "Anion là loại ion mang điện tích gì?", "Mang điện tích âm", "Mang điện tích dương", "Không mang điện", "Lưỡng tính", "A", "Anion là ion mang điện tích âm (-) sinh ra do nguyên tử phi kim nhận electron.");
                    createMapNodeQuestion(node, "Liên kết ion thường hình thành giữa:", "Kim loại điển hình và Phi kim điển hình", "Hai phi kim giống nhau", "Hai kim loại giống nhau", "Khí hiếm và kim loại", "A", "Điển hình là sự kết hợp giữa kim loại mạnh (nhóm IA, IIA) dễ nhường e và phi kim mạnh (nhóm VIA, VIIA) dễ nhận e.");
                    createMapNodeQuestion(node, "Hợp chất nào sau đây có liên kết ion?", "NaCl (Muối ăn)", "CO2", "H2O", "CH4", "A", "NaCl là ví dụ điển hình của liên kết ion hình thành từ Na+ và Cl-.");
                    createMapNodeQuestion(node, "Tính chất vật lý đặc trưng của hợp chất ion là:", "Có nhiệt độ nóng chảy và nhiệt độ sôi cao", "Dễ bay hơi", "Không dẫn điện khi nóng chảy hoặc hòa tan", "Nhiệt độ nóng chảy rất thấp", "A", "Hợp chất ion phân cực mạnh, lực hút ion lớn dẫn đến nhiệt độ nóng chảy và nhiệt độ sôi của chúng rất cao.");
                } else if ("Liên kết cộng hóa trị".equals(name)) {
                    createMapNodeQuestion(node, "Liên kết cộng hóa trị được hình thành bằng cách nào?", "Các nguyên tử góp chung một hoặc nhiều cặp electron", "Sự chuyển electron từ nguyên tử này sang nguyên tử khác", "Lực hút tĩnh điện trái dấu", "Liên kết hydro", "A", "Liên kết cộng hóa trị sinh ra từ sự dùng chung cặp electron giữa hai nguyên tử.");
                    createMapNodeQuestion(node, "Liên kết cộng hóa trị không phân cực xảy ra giữa:", "Hai nguyên tử phi kim giống nhau", "Kim loại mạnh và phi kim mạnh", "Nguyên tử có độ âm điện rất khác biệt", "Hai nguyên tử kim loại", "A", "Hai phi kim giống nhau (như H2, N2, O2) có độ âm điện bằng nhau, cặp e dùng chung không bị lệch.");
                    createMapNodeQuestion(node, "Liên kết cộng hóa trị phân cực xảy ra khi:", "Cặp electron dùng chung bị lệch về phía nguyên tử có độ âm điện lớn hơn", "Không có electron nào được góp chung", "Hai nguyên tử nhường e cho nhau", "Hai kim loại kết hợp", "A", "Lệch về phía nguyên tử có lực hút electron (độ âm điện) mạnh hơn.");
                    createMapNodeQuestion(node, "Trong phân tử nước (H2O), liên kết giữa O và H là loại liên kết gì?", "Liên kết cộng hóa trị phân cực", "Liên kết ion", "Liên kết cộng hóa trị không phân cực", "Liên kết kim loại", "A", "Oxy có độ âm điện lớn hơn Hydro nên cặp electron dùng chung lệch về Oxy, tạo liên kết cộng hóa trị phân cực.");
                    createMapNodeQuestion(node, "Liên kết đôi (hai gạch nối) gồm bao nhiêu cặp electron chung?", "2 cặp electron (4 electron)", "1 cặp electron", "3 cặp electron", "4 cặp electron", "A", "Mỗi gạch nối đại diện cho 1 cặp e chung, liên kết đôi gồm 2 cặp e chung.");
                    createMapNodeQuestion(node, "Liên kết ba trong phân tử khí Nitơ (N2) là liên kết gì?", "Liên kết cộng hóa trị rất bền vững", "Liên kết ion yếu", "Liên kết cộng hóa trị dễ đứt gãy", "Liên kết kim loại", "A", "Liên kết ba của N2 rất bền vững, khiến Nitơ khá trơ ở nhiệt độ phòng.");
                } else if ("Hóa trị và Số oxi hóa".equals(name)) {
                    createMapNodeQuestion(node, "Số oxi hóa của nguyên tố đơn chất (như O2, Fe, H2) luôn bằng bao nhiêu?", "0", "+1", "-1", "+2", "A", "Theo quy tắc xác định số oxi hóa, số oxi hóa của nguyên tố trong đơn chất bằng 0.");
                    createMapNodeQuestion(node, "Trong hợp chất, số oxi hóa của Hydro thường là bao nhiêu?", "+1", "-1", "0", "+2", "A", "Trong đa số hợp chất (trừ hidrua kim loại), số oxi hóa của Hydro là +1.");
                    createMapNodeQuestion(node, "Trong hợp chất, số oxi hóa của Oxy thường là bao nhiêu?", "-2", "+2", "0", "-1", "A", "Trong hầu hết hợp chất (trừ các peroxit, OF2), số oxi hóa của Oxy là -2.");
                    createMapNodeQuestion(node, "Tổng số oxi hóa của các nguyên tử trong một phân tử trung hòa bằng bao nhiêu?", "0", "+1", "-1", "Không xác định", "A", "Trong một phân tử trung hòa về điện, tổng số oxi hóa của các nguyên tố bằng 0.");
                    createMapNodeQuestion(node, "Số oxi hóa của ion đơn nguyên tử (như Na+, Cl-) bằng:", "Đúng bằng điện tích của ion đó", "Luôn bằng 0", "Luôn bằng +1", "Bằng số electron lớp vỏ", "A", "Số oxi hóa của ion đơn nguyên tử bằng điện tích của ion đó (ví dụ Na+ có số oxi hóa +1, Cl- có số oxi hóa -1).");
                    createMapNodeQuestion(node, "Xác định số oxi hóa của Lưu huỳnh (S) trong hợp chất H2SO4?", "+6", "+4", "-2", "0", "A", "H2SO4: H là +1, O là -2. Ta có 2*(+1) + S + 4*(-2) = 0 => S = +6.");
                } else if ("Chúa tể Electron".equals(name)) {
                    createMapNodeQuestion(node, "Hợp chất NaCl nóng chảy có dẫn điện không, và tại sao?", "Có dẫn điện, vì các ion tự do di chuyển tự do", "Không dẫn điện, vì liên kết ion rất chặt chẽ", "Có dẫn điện, do có các electron tự do", "Không dẫn điện, vì NaCl là chất rắn", "A", "Khi nóng chảy, liên kết ion bị phá vỡ tạo thành các cation Na+ và anion Cl- di chuyển tự do dẫn điện.");
                    createMapNodeQuestion(node, "Phân tử nào sau đây chứa liên kết ba?", "N2", "O2", "H2", "H2O", "A", "Khí Nitơ (N2) có liên kết ba bền vững giữa hai nguyên tử N.");
                    createMapNodeQuestion(node, "Xác định số oxi hóa của Nitơ (N) trong phân tử NH3?", "-3", "+3", "0", "+5", "A", "NH3: H có số oxi hóa +1, do đó N + 3*(+1) = 0 => N = -3.");
                    createMapNodeQuestion(node, "Công thức hóa học nào đại diện cho hợp chất cộng hóa trị không phân cực?", "CH4", "HCl", "H2O", "NaCl", "A", "CH4 có độ phân cực hiệu dụng bằng 0, là chất cộng hóa trị không phân cực tiêu biểu.");
                    createMapNodeQuestion(node, "Độ âm điện là gì?", "Khản năng hút electron khi tạo liên kết của một nguyên tử", "Khả năng nhường electron của kim loại", "Năng lượng cần thiết để tách electron ra khỏi vỏ", "Khối lượng của electron trong nguyên tử", "A", "Độ âm điện đặc trưng cho khả năng hút electron của nguyên tử khi hình thành liên kết hóa học.");
                    createMapNodeQuestion(node, "Hiệu độ âm điện lớn hơn hoặc bằng 1,7 thì liên kết là:", "Liên kết ion", "Liên kết cộng hóa trị phân cực", "Liên kết cộng hóa trị không phân cực", "Liên kết hydro", "A", "Theo phân loại lý thuyết, hiệu độ âm điện >= 1.7 hình thành liên kết ion.");
                    createMapNodeQuestion(node, "Trong phân tử HCl, cặp electron dùng chung lệch về phía nguyên tử nào?", "Clo (Cl)", "Hydro (H)", "Nằm chính giữa", "Luân phiên giữa hai bên", "A", "Clo có độ âm điện lớn hơn nhiều so với Hydro nên hút cặp e chung về phía nó.");
                    createMapNodeQuestion(node, "Số oxi hóa của Cacbon trong phân tử CO2 là bao nhiêu?", "+4", "+2", "-4", "0", "A", "CO2: O là -2, do đó C + 2*(-2) = 0 => C = +4.");
                    createMapNodeQuestion(node, "Cấu hình bền vững của khí hiếm có bao nhiêu electron ở lớp ngoài cùng?", "8 electron (hoặc 2 với Heli)", "2 electron", "18 electron", "10 electron", "A", "Cấu hình bền vững bền lâu đạt quy tắc bát tử (8 e ở lớp vỏ ngoài cùng).");
                    createMapNodeQuestion(node, "Chất cộng hóa trị thường có nhiệt độ nóng chảy thấp hơn chất ion vì:", "Lực liên kết giữa các phân tử cộng hóa trị yếu hơn lực hút tĩnh điện mạnh giữa các ion", "Khối lượng phân tử của chúng nhỏ hơn", "Chúng chứa nhiều electron hơn", "Chúng không dẫn điện", "A", "Lực hút tĩnh điện ion mạnh hơn rất nhiều so với lực liên kết liên phân tử yếu giữa các phân tử cộng hóa trị.");
                    createMapNodeQuestion(node, "Xác định số oxi hóa của Mangan (Mn) trong ion KMnO4?", "+7", "+6", "+4", "+2", "A", "KMnO4: K là +1, O là -2. Ta có (+1) + Mn + 4*(-2) = 0 => Mn = +7.");
                    createMapNodeQuestion(node, "Liên kết phối trí (liên kết cho - nhận) là một dạng của liên kết nào?", "Liên kết cộng hóa trị", "Liên kết ion", "Liên kết hydro", "Liên kết kim loại", "A", "Liên kết phối trí là liên kết cộng hóa trị mà cặp electron chung chỉ do một nguyên tử đóng góp.");
                } else if ("Độ pH và Chỉ thị".equals(name)) {
                    createMapNodeQuestion(node, "Thang pH tiêu chuẩn thường có khoảng giá trị từ bao nhiêu?", "0 đến 14", "1 đến 10", "-7 đến +7", "0 đến 7", "A", "Thang đo pH tiêu chuẩn của dung dịch nước nằm trong khoảng 0 đến 14.");
                    createMapNodeQuestion(node, "Dung dịch trung tính (như nước tinh khiết) có pH bằng bao nhiêu?", "7", "Lớn hơn 7", "Nhỏ hơn 7", "0", "A", "Môi trường trung tính ở 25 độ C có pH đúng bằng 7.");
                    createMapNodeQuestion(node, "Dung dịch axit có pH nằm trong khoảng nào?", "Nhỏ hơn 7", "Lớn hơn 7", "Đúng bằng 7", "Lớn hơn 14", "A", "Axit giải phóng ion H+ vào nước, có pH nhỏ hơn 7.");
                    createMapNodeQuestion(node, "Dung dịch bazơ (kiềm) có pH nằm trong khoảng nào?", "Lớn hơn 7", "Nhỏ hơn 7", "Đúng bằng 7", "Bằng 0", "A", "Bazơ thu nhận ion H+ hoặc giải phóng OH-, có pH lớn hơn 7.");
                    createMapNodeQuestion(node, "Giấy quỳ tím chuyển sang màu gì khi gặp dung dịch axit?", "Màu đỏ", "Màu xanh", "Màu vàng", "Không đổi màu", "A", "Quỳ tím gặp axit sẽ đổi sang màu đỏ.");
                    createMapNodeQuestion(node, "Dung dịch phenolphtalein không màu chuyển sang màu gì trong môi trường bazơ?", "Màu hồng (đỏ cánh sen)", "Màu xanh dương", "Màu vàng", "Màu đỏ rực", "A", "Phenolphtalein là chỉ thị nhạy với môi trường bazơ, đổi sang màu hồng đặc trưng.");
                } else if ("Phản ứng Axit-Bazơ".equals(name)) {
                    createMapNodeQuestion(node, "Phản ứng giữa axit và bazơ tạo thành sản phẩm chính là gì?", "Muối và Nước", "Khí Oxy và Nước", "Kim loại và Phi kim", "Axit mới và Bazơ mới", "A", "Phản ứng trung hòa tạo ra sản phẩm gồm muối của kim loại và nước.");
                    createMapNodeQuestion(node, "Phản ứng giữa axit và bazơ được gọi chung là phản ứng gì?", "Phản ứng trung hòa", "Phản ứng phân hủy", "Phản ứng thế", "Phản ứng oxi hóa - khử", "A", "Đây là phản ứng trao đổi ion được gọi là phản ứng trung hòa.");
                    createMapNodeQuestion(node, "Khi cho dung dịch HCl tác dụng với dung dịch NaOH, sản phẩm thu được là:", "NaCl và H2O", "NaCl và H2", "NaClO và H2O", "NaH và Cl2", "A", "HCl + NaOH -> NaCl + H2O.");
                    createMapNodeQuestion(node, "Bản chất của phản ứng trung hòa trong dung dịch nước là sự kết hợp giữa hai ion nào?", "H+ và OH- tạo thành H2O", "Na+ và Cl- tạo NaCl", "H+ và Na+", "OH- và Cl-", "A", "Axit cung cấp H+, bazơ cung cấp OH-, kết hợp tạo ra phân tử nước H2O bền vững.");
                    createMapNodeQuestion(node, "Axit sunfuric H2SO4 phản ứng với đồng(II) hiđroxit Cu(OH)2 tạo muối gì?", "CuSO4", "CuCl2", "CuS", "CuSO3", "A", "H2SO4 + Cu(OH)2 -> CuSO4 + 2H2O.");
                    createMapNodeQuestion(node, "Đặc điểm chung của các bazơ không tan (như Fe(OH)3, Cu(OH)2) là:", "Bị nhiệt phân hủy thành oxit bazơ và nước", "Làm quỳ tím đổi màu xanh", "Tan tốt trong nước", "Có pH lớn hơn 12", "A", "Bazơ không tan bị phân hủy bởi nhiệt sinh ra oxit kim loại tương ứng và nước.");
                } else if ("Vua Thủy Ngân".equals(name)) {
                    createMapNodeQuestion(node, "Kim loại Thủy ngân (Hg) có trạng thái vật lý đặc biệt nào ở nhiệt độ phòng?", "Thể lỏng", "Thể khí", "Thể rắn dẻo", "Thể plasma", "A", "Thủy ngân là kim loại duy nhất tồn tại ở thể lỏng ở nhiệt độ phòng.");
                    createMapNodeQuestion(node, "Axit clohiđric (HCl) tác dụng với kim loại nào sau đây?", "Fe (Sắt)", "Cu (Đồng)", "Ag (Bạc)", "Au (Vàng)", "A", "HCl chỉ tác dụng với kim loại đứng trước Hydro trong dãy hoạt động hóa học (Fe đứng trước H, Cu, Ag, Au đứng sau H).");
                    createMapNodeQuestion(node, "Dung dịch nước chanh có pH khoảng 2.0, đây là môi trường gì?", "Môi trường axit mạnh", "Môi trường bazơ mạnh", "Môi trường trung tính", "Môi trường kiềm yếu", "A", "pH khoảng 2.0 nhỏ hơn 7 rất nhiều, là môi trường axit mạnh.");
                    createMapNodeQuestion(node, "Chất nào được dùng để trung hòa đất bị chua (nhiều axit) trong nông nghiệp?", "Vôi tôi Ca(OH)2 hoặc vôi sống CaO", "Thạch cao CaSO4", "Muối ăn NaCl", "Axit clohidric HCl", "A", "Ca(OH)2 là bazơ rẻ tiền được nông dân dùng để rải ruộng khử chua axit cho đất ruộng.");
                    createMapNodeQuestion(node, "Khí nào sinh ra khi cho kim loại Kẽm (Zn) vào dung dịch axit HCl?", "Khí Hydro (H2)", "Khí Clo (Cl2)", "Khí Oxy (O2)", "Khí Cacbonic (CO2)", "A", "Zn + 2HCl -> ZnCl2 + H2 (khí thoát ra bay lên).");
                    createMapNodeQuestion(node, "Sản phẩm thu được khi cho CO2 tác dụng với dung dịch bazơ Ca(OH)2 dư là:", "Kết tủa trắng CaCO3 và H2O", "CaO và H2O", "Khí CO và H2", "Ca(HCO3)2 không màu", "A", "CO2 + Ca(OH)2 -> CaCO3 (kết tủa trắng) + H2O.");
                    createMapNodeQuestion(node, "Chất chỉ thị màu vạn năng đổi màu gì khi dung dịch có pH cực kỳ bazơ (pH = 13-14)?", "Màu tím đậm hoặc xanh đậm", "Màu đỏ rực", "Màu vàng nhạt", "Màu cam", "A", "Ở môi trường bazơ cực mạnh, chất chỉ thị vạn năng chuyển sang màu xanh đậm hoặc tím.");
                    createMapNodeQuestion(node, "Một dung dịch có nồng độ ion H+ là 10^-5 M thì có pH bằng bao nhiêu?", "5", "9", "14", "-5", "A", "pH = -log[H+] = -log(10^-5) = 5.");
                    createMapNodeQuestion(node, "Cho kim loại Natri (Na) vào nước thu được dung dịch có pH thế nào?", "pH > 7 (dung dịch bazơ NaOH)", "pH < 7 (dung dịch axit)", "pH = 7 (nước muối)", "pH = 0", "A", "Na + H2O -> NaOH (bazơ mạnh) + 1/2 H2, NaOH tạo môi trường kiềm có pH > 7.");
                    createMapNodeQuestion(node, "Nhỏ dung dịch BaCl2 vào dung dịch H2SO4 xuất hiện hiện tượng gì?", "Xuất hiện kết tủa trắng không tan trong axit", "Dung dịch đổi màu xanh", "Có bọt khí bay lên", "Dung dịch tỏa nhiệt mạnh và chuyển đỏ", "A", "BaCl2 + H2SO4 -> BaSO4 (kết tủa trắng bền vững) + 2HCl.");
                    createMapNodeQuestion(node, "Để đo chỉ số pH của dung dịch một cách chính xác nhất, người ta sử dụng thiết bị gì?", "Máy đo pH điện tử (pH meter)", "Giấy quỳ tím", "Phenolphtalein", "Nhiệt kế", "A", "Máy đo pH điện tử cho chỉ số pH chính xác bằng điện cực, vượt trội so với giấy chỉ thị màu ước lượng.");
                    createMapNodeQuestion(node, "Phản ứng giữa axit axetic (CH3COOH) và muối Na2CO3 giải phóng khí gì?", "Khí Cacbonic (CO2)", "Khí Hydro (H2)", "Khí Clo (Cl2)", "Khí Oxy (O2)", "A", "Axit tác dụng với muối cacbonat sinh ra khí cacbonic CO2 bay ra sủi bọt.");
                }
            }
        }
    }

    private void createMapNodeQuestion(MapNode node, String prompt, String a, String b, String c, String d, String correct, String explanation) {
        MapNodeQuestion q = new MapNodeQuestion();
        q.setNode(node);
        q.setPrompt(prompt);
        q.setOptionA(a);
        q.setOptionB(b);
        q.setOptionC(c);
        q.setOptionD(d);
        q.setCorrectOption(correct);
        q.setExplanation(explanation);
        mapNodeQuestionRepository.save(q);
    }

    private void seedInventoryItems() {
        if (inventoryRepository.count() > 0) return;

        List<InventoryItem> items = new ArrayList<>();

        InventoryItem beaker = new InventoryItem();
        beaker.setItemCode("beaker");
        beaker.setName("Cốc thủy tinh");
        beaker.setType(com.example.chemlearn.lab.enums.ItemType.CONTAINER);
        beaker.setIconName("Beaker");
        beaker.setIconColor("text-blue-500");
        items.add(beaker);

        InventoryItem testTube = new InventoryItem();
        testTube.setItemCode("test_tube");
        testTube.setName("Ống nghiệm");
        testTube.setType(com.example.chemlearn.lab.enums.ItemType.CONTAINER);
        testTube.setIconName("TestTube");
        testTube.setIconColor("text-sky-400");
        items.add(testTube);

        InventoryItem bunsenBurner = new InventoryItem();
        bunsenBurner.setItemCode("bunsen_burner");
        bunsenBurner.setName("Đèn cồn");
        bunsenBurner.setType(com.example.chemlearn.lab.enums.ItemType.EQUIPMENT);
        bunsenBurner.setIconName("Flame");
        bunsenBurner.setIconColor("text-orange-500");
        items.add(bunsenBurner);

        InventoryItem sodium = new InventoryItem();
        sodium.setItemCode("sodium");
        sodium.setName("Natri (Na)");
        sodium.setType(com.example.chemlearn.lab.enums.ItemType.CHEMICAL);
        sodium.setState(com.example.chemlearn.lab.enums.PhysicalState.SOLID);
        sodium.setSubCategory(com.example.chemlearn.lab.enums.SubCategory.METAL);
        sodium.setIconName("Square");
        sodium.setIconColor("text-slate-300");
        sodium.setIconFill("#cbd5e1"); // Slate 300
        items.add(sodium);

        InventoryItem copper = new InventoryItem();
        copper.setItemCode("copper");
        copper.setName("Đồng (Cu)");
        copper.setType(com.example.chemlearn.lab.enums.ItemType.CHEMICAL);
        copper.setState(com.example.chemlearn.lab.enums.PhysicalState.SOLID);
        copper.setSubCategory(com.example.chemlearn.lab.enums.SubCategory.METAL);
        copper.setIconName("Square");
        copper.setIconColor("text-orange-700");
        copper.setIconFill("#c2410c"); // Orange 700
        items.add(copper);

        InventoryItem fePowder = new InventoryItem();
        fePowder.setItemCode("fe_powder");
        fePowder.setName("Bột sắt (Fe)");
        fePowder.setType(com.example.chemlearn.lab.enums.ItemType.CHEMICAL);
        fePowder.setState(com.example.chemlearn.lab.enums.PhysicalState.SOLID);
        fePowder.setSubCategory(com.example.chemlearn.lab.enums.SubCategory.METAL);
        fePowder.setIconName("CircleDot");
        fePowder.setIconColor("text-gray-600");
        fePowder.setIconFill("#4b5563"); // Gray 600
        items.add(fePowder);

        InventoryItem znGrain = new InventoryItem();
        znGrain.setItemCode("zn_grain");
        znGrain.setName("Kẽm (Zn)");
        znGrain.setType(com.example.chemlearn.lab.enums.ItemType.CHEMICAL);
        znGrain.setState(com.example.chemlearn.lab.enums.PhysicalState.SOLID);
        znGrain.setSubCategory(com.example.chemlearn.lab.enums.SubCategory.METAL);
        znGrain.setIconName("CircleDot");
        znGrain.setIconColor("text-slate-400");
        znGrain.setIconFill("#94a3b8"); // Slate 400
        items.add(znGrain);

        InventoryItem caco3 = new InventoryItem();
        caco3.setItemCode("caco3");
        caco3.setName("Đá vôi (CaCO3)");
        caco3.setType(com.example.chemlearn.lab.enums.ItemType.CHEMICAL);
        caco3.setState(com.example.chemlearn.lab.enums.PhysicalState.SOLID);
        caco3.setSubCategory(com.example.chemlearn.lab.enums.SubCategory.SALT_SOLID);
        caco3.setIconName("Square");
        caco3.setIconColor("text-stone-200");
        caco3.setIconFill("#e7e5e4"); // Stone 200
        items.add(caco3);

        com.example.chemlearn.lab.entity.InventoryItem kmno4 = new com.example.chemlearn.lab.entity.InventoryItem();
        kmno4.setItemCode("kmno4");
        kmno4.setName("Thuốc tím (KMnO4)");
        kmno4.setType(com.example.chemlearn.lab.enums.ItemType.CHEMICAL);
        kmno4.setState(com.example.chemlearn.lab.enums.PhysicalState.SOLID);
        kmno4.setSubCategory(com.example.chemlearn.lab.enums.SubCategory.SALT_SOLID);
        kmno4.setIconName("CircleDot");
        kmno4.setIconColor("text-fuchsia-800");
        kmno4.setIconFill("#86198f"); // Fuchsia 800
        items.add(kmno4);

        InventoryItem nacl = new InventoryItem();
        nacl.setItemCode("nacl");
        nacl.setName("Muối ăn (NaCl)");
        nacl.setType(com.example.chemlearn.lab.enums.ItemType.CHEMICAL);
        nacl.setState(com.example.chemlearn.lab.enums.PhysicalState.SOLID);
        nacl.setSubCategory(com.example.chemlearn.lab.enums.SubCategory.SALT_SOLID);
        nacl.setIconName("CircleDot");
        nacl.setIconColor("text-white");
        nacl.setIconFill("#ffffff"); // White
        items.add(nacl);

        InventoryItem na2co3 = new InventoryItem();
        na2co3.setItemCode("na2co3");
        na2co3.setName("Natri Cacbonat");
        na2co3.setType(com.example.chemlearn.lab.enums.ItemType.CHEMICAL);
        na2co3.setState(com.example.chemlearn.lab.enums.PhysicalState.SOLID);
        na2co3.setSubCategory(com.example.chemlearn.lab.enums.SubCategory.SALT_SOLID);
        na2co3.setIconName("CircleDot");
        na2co3.setIconColor("text-white");
        na2co3.setIconFill("#f8fafc"); // Slate 50
        items.add(na2co3);

        InventoryItem cao = new InventoryItem();
        cao.setItemCode("cao");
        cao.setName("Vôi sống (CaO)");
        cao.setType(com.example.chemlearn.lab.enums.ItemType.CHEMICAL);
        cao.setState(com.example.chemlearn.lab.enums.PhysicalState.SOLID);
        cao.setSubCategory(com.example.chemlearn.lab.enums.SubCategory.OXIDE);
        cao.setIconName("Square");
        cao.setIconColor("text-stone-300");
        cao.setIconFill("#d6d3d1"); // Stone 300
        items.add(cao);

        InventoryItem water = new InventoryItem();
        water.setItemCode("water");
        water.setName("Nước cất");
        water.setType(com.example.chemlearn.lab.enums.ItemType.CHEMICAL);
        water.setState(com.example.chemlearn.lab.enums.PhysicalState.LIQUID);
        water.setSubCategory(com.example.chemlearn.lab.enums.SubCategory.SOLVENT);
        water.setIconName("Droplet");
        water.setIconColor("text-blue-300");
        items.add(water);

        InventoryItem hcl = new InventoryItem();
        hcl.setItemCode("hcl");
        hcl.setName("Axit HCl");
        hcl.setType(com.example.chemlearn.lab.enums.ItemType.CHEMICAL);
        hcl.setState(com.example.chemlearn.lab.enums.PhysicalState.LIQUID);
        hcl.setSubCategory(com.example.chemlearn.lab.enums.SubCategory.ACID);
        hcl.setIconName("Droplet");
        hcl.setIconColor("text-stone-200");
        items.add(hcl);

        InventoryItem hclDac = new InventoryItem();
        hclDac.setItemCode("hcl_dac");
        hclDac.setName("Axit HCl (Đặc)");
        hclDac.setType(com.example.chemlearn.lab.enums.ItemType.CHEMICAL);
        hclDac.setState(com.example.chemlearn.lab.enums.PhysicalState.LIQUID);
        hclDac.setSubCategory(com.example.chemlearn.lab.enums.SubCategory.ACID);
        hclDac.setIconName("Droplet");
        hclDac.setIconColor("text-red-500");
        hclDac.setIconFill("currentColor");
        items.add(hclDac);

        InventoryItem h2so4Dac = new InventoryItem();
        h2so4Dac.setItemCode("h2so4_dac");
        h2so4Dac.setName("Axit H2SO4 (Đặc)");
        h2so4Dac.setType(com.example.chemlearn.lab.enums.ItemType.CHEMICAL);
        h2so4Dac.setState(com.example.chemlearn.lab.enums.PhysicalState.LIQUID);
        h2so4Dac.setSubCategory(com.example.chemlearn.lab.enums.SubCategory.ACID);
        h2so4Dac.setIconName("Droplet");
        h2so4Dac.setIconColor("text-red-600");
        h2so4Dac.setIconFill("currentColor");
        items.add(h2so4Dac);

        InventoryItem h2c2o4 = new InventoryItem();
        h2c2o4.setItemCode("h2c2o4");
        h2c2o4.setName("Axit Oxalic");
        h2c2o4.setType(com.example.chemlearn.lab.enums.ItemType.CHEMICAL);
        h2c2o4.setState(com.example.chemlearn.lab.enums.PhysicalState.LIQUID);
        h2c2o4.setSubCategory(com.example.chemlearn.lab.enums.SubCategory.ACID);
        h2c2o4.setIconName("Droplet");
        h2c2o4.setIconColor("text-stone-200");
        items.add(h2c2o4);

        InventoryItem naohSol = new InventoryItem();
        naohSol.setItemCode("naoh_sol");
        naohSol.setName("Dung dịch NaOH");
        naohSol.setType(com.example.chemlearn.lab.enums.ItemType.CHEMICAL);
        naohSol.setState(com.example.chemlearn.lab.enums.PhysicalState.LIQUID);
        naohSol.setSubCategory(com.example.chemlearn.lab.enums.SubCategory.ALKALI);
        naohSol.setIconName("Droplet");
        naohSol.setIconColor("text-stone-200");
        items.add(naohSol);

        InventoryItem cuso4 = new InventoryItem();
        cuso4.setItemCode("cuso4");
        cuso4.setName("Dung dịch CuSO4");
        cuso4.setType(com.example.chemlearn.lab.enums.ItemType.CHEMICAL);
        cuso4.setState(com.example.chemlearn.lab.enums.PhysicalState.LIQUID);
        cuso4.setSubCategory(com.example.chemlearn.lab.enums.SubCategory.SALT_SOLUTION);
        cuso4.setIconName("Droplet");
        cuso4.setIconColor("text-blue-500");
        cuso4.setIconFill("currentColor");
        items.add(cuso4);

        InventoryItem bacl2 = new InventoryItem();
        bacl2.setItemCode("bacl2");
        bacl2.setName("Dung dịch BaCl2");
        bacl2.setType(com.example.chemlearn.lab.enums.ItemType.CHEMICAL);
        bacl2.setState(com.example.chemlearn.lab.enums.PhysicalState.LIQUID);
        bacl2.setSubCategory(com.example.chemlearn.lab.enums.SubCategory.SALT_SOLUTION);
        bacl2.setIconName("Droplet");
        bacl2.setIconColor("text-stone-200");
        items.add(bacl2);

        InventoryItem na2so4 = new InventoryItem();
        na2so4.setItemCode("na2so4");
        na2so4.setName("Dung dịch Na2SO4");
        na2so4.setType(com.example.chemlearn.lab.enums.ItemType.CHEMICAL);
        na2so4.setState(com.example.chemlearn.lab.enums.PhysicalState.LIQUID);
        na2so4.setSubCategory(com.example.chemlearn.lab.enums.SubCategory.SALT_SOLUTION);
        na2so4.setIconName("Droplet");
        na2so4.setIconColor("text-stone-200");
        items.add(na2so4);

        InventoryItem fecl3 = new InventoryItem();
        fecl3.setItemCode("fecl3");
        fecl3.setName("Dung dịch FeCl3");
        fecl3.setType(com.example.chemlearn.lab.enums.ItemType.CHEMICAL);
        fecl3.setState(com.example.chemlearn.lab.enums.PhysicalState.LIQUID);
        fecl3.setSubCategory(com.example.chemlearn.lab.enums.SubCategory.SALT_SOLUTION);
        fecl3.setIconName("Droplet");
        fecl3.setIconColor("text-amber-600");
        fecl3.setIconFill("currentColor");
        items.add(fecl3);

        InventoryItem agno3 = new InventoryItem();
        agno3.setItemCode("agno3");
        agno3.setName("Dung dịch AgNO3");
        agno3.setType(com.example.chemlearn.lab.enums.ItemType.CHEMICAL);
        agno3.setState(com.example.chemlearn.lab.enums.PhysicalState.LIQUID);
        agno3.setSubCategory(com.example.chemlearn.lab.enums.SubCategory.SALT_SOLUTION);
        agno3.setIconName("Droplet");
        agno3.setIconColor("text-stone-200");
        items.add(agno3);

        InventoryItem phenolphthalein = new InventoryItem();
        phenolphthalein.setItemCode("phenolphthalein");
        phenolphthalein.setName("Phenolphtalein");
        phenolphthalein.setType(com.example.chemlearn.lab.enums.ItemType.CHEMICAL);
        phenolphthalein.setState(com.example.chemlearn.lab.enums.PhysicalState.LIQUID);
        phenolphthalein.setSubCategory(com.example.chemlearn.lab.enums.SubCategory.INDICATOR);
        phenolphthalein.setIconName("Droplet");
        phenolphthalein.setIconColor("text-stone-200");
        items.add(phenolphthalein);

        InventoryItem litmusPaper = new InventoryItem();
        litmusPaper.setItemCode("litmus_paper");
        litmusPaper.setName("Giấy quỳ tím");
        litmusPaper.setType(com.example.chemlearn.lab.enums.ItemType.CHEMICAL);
        litmusPaper.setState(com.example.chemlearn.lab.enums.PhysicalState.SOLID);
        litmusPaper.setSubCategory(com.example.chemlearn.lab.enums.SubCategory.INDICATOR);
        litmusPaper.setIconName("Square");
        litmusPaper.setIconColor("text-purple-300");
        litmusPaper.setIconFill("#d8b4fe"); // Purple 300
        items.add(litmusPaper);

        inventoryRepository.saveAll(items);
    }

    private void updateInventoryIconFills() {
        List<InventoryItem> allItems = inventoryRepository.findAll();
        for (InventoryItem item : allItems) {
            switch (item.getItemCode()) {
                // Rắn (Solid)
                case "sodium": item.setIconFill("#cbd5e1"); break;
                case "copper": item.setIconFill("#c2410c"); break;
                case "fe_powder": item.setIconFill("#4b5563"); break;
                case "zn_grain": item.setIconFill("#94a3b8"); break;
                case "caco3": item.setIconFill("#e7e5e4"); break;
                case "kmno4": item.setIconFill("#86198f"); break;
                case "nacl": item.setIconFill("#ffffff"); break;
                case "na2co3": item.setIconFill("#f8fafc"); break;
                case "cao": item.setIconFill("#d6d3d1"); break;
                case "litmus_paper": item.setIconFill("#d8b4fe"); break;
                
                // Lỏng (Liquid)
                case "water": item.setIconFill("#60a5fa"); break; // Blue 400
                case "hcl": item.setIconFill("#f1f5f9"); break; // Slate 100 (Trong suốt)
                case "hcl_dac": item.setIconFill("#f1f5f9"); break;
                case "h2so4_dac": item.setIconFill("#f1f5f9"); break;
                case "h2c2o4": item.setIconFill("#f1f5f9"); break;
                case "naoh_sol": item.setIconFill("#f1f5f9"); break;
                case "cuso4": item.setIconFill("#3b82f6"); break; // Blue 500
                case "bacl2": item.setIconFill("#f1f5f9"); break;
                case "na2so4": item.setIconFill("#f1f5f9"); break;
                case "fecl3": item.setIconFill("#d97706"); break; // Amber 600
                case "agno3": item.setIconFill("#f1f5f9"); break;
                case "phenolphthalein": item.setIconFill("#f1f5f9"); break;
            }
        }
        inventoryRepository.saveAll(allItems);
    }
}
