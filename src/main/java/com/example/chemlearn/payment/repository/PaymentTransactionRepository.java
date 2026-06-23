package com.example.chemlearn.payment.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.chemlearn.payment.entity.PaymentTransaction;
import com.example.chemlearn.payment.enums.PaymentStatus;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {
    Optional<PaymentTransaction> findByOrderCode(Long orderCode);

    boolean existsByOrderCode(Long orderCode);

    Optional<PaymentTransaction> findTopByUserIdAndPackageCodeAndStatusOrderByCreatedAtDesc(
            UUID userId,
            String packageCode,
            PaymentStatus status
    );
}
