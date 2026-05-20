package com.example.chemlearn.lms.service;

import com.example.chemlearn.core.entity.Parent;
import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.lms.entity.ParentStudentLink;
import com.example.chemlearn.lms.repository.ParentRepository;
import com.example.chemlearn.lms.repository.ParentStudentLinkRepository;
import com.example.chemlearn.lms.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParentStudentLinkService {

    private final ParentStudentLinkRepository parentStudentLinkRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;

    /**
     * Runs in its own transaction (REQUIRES_NEW).
     * If the link already exists (duplicate key), only this inner transaction
     * rolls back — the caller's transaction remains unaffected.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensureLinkExists(User parentUser, User studentUser, UUID parentUserId, UUID studentUserId) {
        Optional<ParentStudentLink> existing = parentStudentLinkRepository
                .findByParent_IdAndStudent_Id(parentUserId, studentUserId);

        if (existing.isPresent()) {
            log.info("ParentStudentLink already exists, skipping insert.");
            return;
        }

        ParentStudentLink link = new ParentStudentLink();
        link.setParent(parentUser);
        link.setStudent(studentUser);
        parentStudentLinkRepository.save(link);

        // Also sync into Student.parent field
        studentRepository.findByUsers_Id(studentUserId).ifPresent(student -> {
            parentRepository.findByUsers_Id(parentUserId).ifPresent(parent -> {
                student.setParent(parent);
                studentRepository.save(student);
            });
        });

        log.info("Created ParentStudentLink: parent={} student={}", parentUserId, studentUserId);
    }
}
