package com.example.chemlearn.payment.service.impl;

import com.example.chemlearn.lms.repository.UserRepository;
import com.example.chemlearn.payment.dto.LearningPackageResponse;
import com.example.chemlearn.payment.entity.LearningPackage;
import com.example.chemlearn.payment.repository.LearningPackageRepository;
import com.example.chemlearn.payment.repository.PaymentTransactionRepository;
import com.example.chemlearn.payment.repository.UserPackageEntitlementRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import vn.payos.PayOS;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaymentServiceImplTest {

    private final PaymentTransactionRepository paymentTransactionRepository = mock(PaymentTransactionRepository.class);
    private final LearningPackageRepository learningPackageRepository = mock(LearningPackageRepository.class);
    private final UserPackageEntitlementRepository entitlementRepository = mock(UserPackageEntitlementRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final PayOS payOS = mock(PayOS.class);
    private final EntityManager entityManager = mock(EntityManager.class);

    private final PaymentServiceImpl service = new PaymentServiceImpl(
            paymentTransactionRepository,
            learningPackageRepository,
            entitlementRepository,
            userRepository,
            payOS,
            new ObjectMapper(),
            entityManager
    );

    @Test
    void activePackageListingUsesOnlyActiveRepositoryResult() {
        LearningPackage grade6 = learningPackage("GRADE_6", 6, "Chemlearn edu 6", true);
        LearningPackage grade7 = learningPackage("GRADE_7", 7, "Chemlearn edu 7", true);
        when(learningPackageRepository.findAllByIsActiveTrueOrderByGradeLevelAsc()).thenReturn(List.of(grade6, grade7));

        List<LearningPackageResponse> packages = service.getActivePackages();

        assertThat(packages).extracting(LearningPackageResponse::getPackageCode)
                .containsExactly("GRADE_6", "GRADE_7");
        assertThat(packages).extracting(LearningPackageResponse::getPackageName)
                .containsExactly("Chemlearn edu 6", "Chemlearn edu 7");
    }

    private LearningPackage learningPackage(String code, Integer gradeLevel, String name, boolean active) {
        LearningPackage learningPackage = new LearningPackage();
        learningPackage.setPackageCode(code);
        learningPackage.setGradeLevel(gradeLevel);
        learningPackage.setPackageName(name);
        learningPackage.setBasePrice(400000L);
        learningPackage.setDurationDays(365);
        learningPackage.setIsActive(active);
        return learningPackage;
    }
}
