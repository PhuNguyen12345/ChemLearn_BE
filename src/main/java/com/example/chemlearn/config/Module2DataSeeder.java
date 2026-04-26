//package com.example.chemlearn.config;
//
//import com.example.chemlearn.core.entity.Account;
//import com.example.chemlearn.core.entity.Teacher;
//import com.example.chemlearn.lms.entity.*;
//import com.example.chemlearn.lms.enums.AccountRole;
//import com.example.chemlearn.lms.enums.AssignmentStatus;
//import com.example.chemlearn.lms.enums.QuizType;
//import com.example.chemlearn.lms.repository.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.time.Instant;
//
//import static com.example.chemlearn.util.PasswordUtil.hash;
//
//@Configuration
//@RequiredArgsConstructor
//public class Module2DataSeeder {
//
//    private final AccountRepository accountRepository;
//    private final ChapterRepository chapterRepository;
//    private final LessonRepository lessonRepository;
//    private final MiniQuizQuestionRepository miniQuizQuestionRepository;
//    private final QuizRepository quizRepository;
//    private final QuizQuestionRepository quizQuestionRepository;
//    private final AssignmentRepository assignmentRepository;
//    private final ParentStudentLinkRepository parentStudentLinkRepository;
//
//    @Bean
//    public CommandLineRunner seedModule2Data() {
//        return args -> {
//            Account teacher = accountRepository.findByUsername("teacher_m2")
//                    .orElseGet(() -> {
//                        Account account = new Account();
//                        account.setUsername("teacher_m2");
//                        account.setEmail("teacher.m2@chemlearn.local");
//                        account.setPassword(hash("Teacher@123"));
//                        account.setRole(AccountRole.ROLE_TEACHER);
//                        account.setEnabled(true);
//                        return accountRepository.save(account);
//                    });
//
//            Account student = accountRepository.findByUsername("student_m2")
//                    .orElseGet(() -> {
//                        Account account = new Account();
//                        account.setUsername("student_m2");
//                        account.setEmail("student.m2@chemlearn.local");
//                        account.setPassword(hash("Student@123"));
//                        account.setRole(AccountRole.ROLE_STUDENT);
//                        account.setEnabled(true);
//                        return accountRepository.save(account);
//                    });
//
//            Account parent = accountRepository.findByUsername("parent_m2")
//                    .orElseGet(() -> {
//                        Account account = new Account();
//                        account.setUsername("parent_m2");
//                        account.setEmail("parent.m2@chemlearn.local");
//                        account.setPassword(hash("Parent@123"));
//                        account.setRole(AccountRole.ROLE_PARENT);
//                        account.setEnabled(true);
//                        return accountRepository.save(account);
//                    });
//
//            parentStudentLinkRepository.findByParentIdAndStudentId(parent.getId(), student.getId())
//                    .orElseGet(() -> {
//                        ParentStudentLink link = new ParentStudentLink();
//                        link.setParent(parent);
//                        link.setStudent(student);
//                        return parentStudentLinkRepository.save(link);
//                    });
//
//            if (chapterRepository.count() == 0) {
//                Chapter chapter1 = new Chapter();
//                chapter1.setTitle("Atomic Structure Foundations");
//                chapter1.setDescription("Core ideas about atoms, isotopes, and subatomic particles.");
//                chapter1.setOrderIndex(1);
//                chapterRepository.save(chapter1);
//
//                Lesson lesson1 = new Lesson();
//                lesson1.setChapter(chapter1);
//                lesson1.setContentType("text");
//                lesson1.setTitle("Protons, Neutrons, and Electrons");
//                lesson1.setDurationMinutes(10);
//                lesson1.setOrderIndex(1);
//                lesson1.setTextContent("Atoms are made of protons and neutrons in the nucleus, with electrons in orbitals around it. The atomic number is the number of protons and determines the element identity.");
//                lessonRepository.save(lesson1);
//
//                MiniQuizQuestion miniQ1 = new MiniQuizQuestion();
//                miniQ1.setLesson(lesson1);
//                miniQ1.setPrompt("If an atom has 8 protons and mass number 16, how many neutrons does it have?");
//                miniQ1.setOptionA("6");
//                miniQ1.setOptionB("8");
//                miniQ1.setOptionC("10");
//                miniQ1.setOptionD("16");
//                miniQ1.setCorrectOption("B");
//                miniQ1.setExplanation("Mass number = protons + neutrons, so neutrons = 16 - 8 = 8.");
//                miniQuizQuestionRepository.save(miniQ1);
//            }
//
//            Quiz freeQuiz;
//            if (quizRepository.count() == 0) {
//                freeQuiz = new Quiz();
//                freeQuiz.setTitle("Atomic Structure Quick Check");
//                freeQuiz.setDescription("Practice identifying particle counts and periodic trends.");
//                freeQuiz.setQuizType(QuizType.FREE);
//                freeQuiz.setDurationMinutes(12);
//                freeQuiz.setPublished(true);
//                freeQuiz.setCreatedBy(new Teacher());
//                quizRepository.save(freeQuiz);
//
//                QuizQuestion q1 = new QuizQuestion();
//                q1.setQuiz(freeQuiz);
//                q1.setPrompt("What particle determines the identity of an element?");
//                q1.setOptionA("Electron");
//                q1.setOptionB("Neutron");
//                q1.setOptionC("Proton");
//                q1.setOptionD("Photon");
//                q1.setCorrectOption("C");
//                q1.setDisplayOrder(1);
//                q1.setExplanation("The number of protons defines the atomic number and element identity.");
//                quizQuestionRepository.save(q1);
//
//                QuizQuestion q2 = new QuizQuestion();
//                q2.setQuiz(freeQuiz);
//                q2.setPrompt("An atom has atomic number 11. How many electrons does a neutral atom have?");
//                q2.setOptionA("9");
//                q2.setOptionB("11");
//                q2.setOptionC("13");
//                q2.setOptionD("22");
//                q2.setCorrectOption("B");
//                q2.setDisplayOrder(2);
//                q2.setExplanation("Neutral atoms have equal protons and electrons.");
//                quizQuestionRepository.save(q2);
//            } else {
//                freeQuiz = quizRepository.findAll().stream().findFirst().orElse(null);
//            }
//
//            if (assignmentRepository.count() == 0 && freeQuiz != null) {
//                Assignment assignment = new Assignment();
//                assignment.setTitle("Atomic Structure Homework #1");
//                assignment.setQuiz(freeQuiz);
//                assignment.setTeacher(teacher);
//                assignment.setStudent(student);
//                assignment.setDueAt(Instant.now().plusSeconds(604800)); //1 week later
//                assignment.setStatus(AssignmentStatus.PUBLISHED);
//                assignmentRepository.save(assignment);
//            }
//        };
//    }
//}
