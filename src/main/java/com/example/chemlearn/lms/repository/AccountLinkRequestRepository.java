package com.example.chemlearn.lms.repository;

import com.example.chemlearn.lms.entity.AccountLinkRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountLinkRequestRepository extends JpaRepository<AccountLinkRequest, UUID> {
    Optional<AccountLinkRequest> findByToken(String token);
    List<AccountLinkRequest> findByInitiatorIdAndStatus(UUID initiatorId, String status);
    List<AccountLinkRequest> findByTargetEmailAndStatus(String targetEmail, String status);
    Optional<AccountLinkRequest> findByInitiatorIdAndTargetEmailAndStatus(UUID initiatorId, String targetEmail, String status);
}
