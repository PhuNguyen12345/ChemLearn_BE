package com.example.chemlearn.payment.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.chemlearn.payment.entity.UserPackageEntitlement;
import com.example.chemlearn.payment.enums.EntitlementStatus;

@Repository
public interface UserPackageEntitlementRepository extends JpaRepository<UserPackageEntitlement, UUID> {
    Optional<UserPackageEntitlement> findByPaymentTransactionId(UUID paymentTransactionId);

    List<UserPackageEntitlement> findByUserIdAndStatus(UUID userId, EntitlementStatus status);

    Optional<UserPackageEntitlement> findByUserIdAndPackageCodeAndStatus(UUID userId, String packageCode, EntitlementStatus status);
}