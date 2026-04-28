package com.example.chemlearn.config;

import com.example.chemlearn.core.entity.Teacher;
import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.lms.entity.*;
import com.example.chemlearn.lms.enums.AssignmentStatus;
import com.example.chemlearn.lms.enums.QuizType;
import com.example.chemlearn.lms.repository.*;
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

    @Bean
    public CommandLineRunner seedModule2Data() {
        return args -> {
            User admin = accountRepository.findByUsername("admin")
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

//            parentStudentLinkRepository.findByParentIdAndStudentId(parent.getId(), student.getId())
//                    .orElseGet(() -> {
//                        ParentStudentLink link = new ParentStudentLink();
//                        link.setParent(parent);
//                        link.setStudent(student);
//                        return parentStudentLinkRepository.save(link);
//                    });
//
        };
    }
}
