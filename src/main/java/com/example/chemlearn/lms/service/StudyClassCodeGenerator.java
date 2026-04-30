package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.repository.StudyClassRepository;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyClassCodeGenerator {
    private static final int MAX_CODE = 1_000_000;

    private final StudyClassRepository studyClassRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateUniqueCode() {
        String code;
        do {
            code = String.format("%06d", secureRandom.nextInt(MAX_CODE));
        } while (studyClassRepository.existsByClassCode(code));
        return code;
    }
}