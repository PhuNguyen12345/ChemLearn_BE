package com.example.chemlearn;

import com.example.chemlearn.lms.repository.UserRepository;
import com.example.chemlearn.lms.repository.ParentStudentLinkRepository;
import com.example.chemlearn.core.entity.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DbCheckRunner implements CommandLineRunner {
    private final UserRepository userRepository;
    private final ParentStudentLinkRepository parentStudentLinkRepository;

    @Override
    @Transactional(readOnly = true)
    public void run(String... args) {
        log.info("Checking DB content...");
        userRepository.findByUsername("parent1").ifPresent(parent -> {
            log.info("Found parent1: {}", parent.getId());
            var links = parentStudentLinkRepository.findByParent_Id(parent.getId());
            log.info("Found {} links for parent1", links.size());
            for (var link : links) {
                log.info("Link: student={}", link.getStudent().getUsername());
            }
        });
    }
}
