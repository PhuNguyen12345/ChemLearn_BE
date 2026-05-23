package com.example.chemlearn.config;

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
import com.example.chemlearn.gamification.repository.MapIslandRepository;
import com.example.chemlearn.gamification.repository.MapNodeRepository;
import com.example.chemlearn.lms.entity.Chapter;
import com.example.chemlearn.lms.entity.ClassStudentLink;
import com.example.chemlearn.lms.entity.Lesson;
import com.example.chemlearn.lms.entity.MiniQuizQuestion;
import com.example.chemlearn.lms.entity.ParentStudentLink;
import com.example.chemlearn.lms.entity.QuestionBankItem;
import com.example.chemlearn.lms.entity.StudyClass;
import com.example.chemlearn.lms.enums.MaterialScope;
import com.example.chemlearn.lms.enums.QuestionType;
import com.example.chemlearn.lms.repository.ChapterRepository;
import com.example.chemlearn.lms.repository.ClassStudentLinkRepository;
import com.example.chemlearn.lms.repository.LessonRepository;
import com.example.chemlearn.lms.repository.MiniQuizQuestionRepository;
import com.example.chemlearn.lms.repository.ParentRepository;
import com.example.chemlearn.lms.repository.ParentStudentLinkRepository;
import com.example.chemlearn.lms.repository.QuestionBankItemRepository;
import com.example.chemlearn.lms.repository.StudentRepository;
import com.example.chemlearn.lms.repository.StudyClassRepository;
import com.example.chemlearn.lms.repository.TeacherRepository;
import com.example.chemlearn.lms.repository.UserRepository;
import static com.example.chemlearn.util.PasswordUtil.hash;

import lombok.RequiredArgsConstructor;

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
}
