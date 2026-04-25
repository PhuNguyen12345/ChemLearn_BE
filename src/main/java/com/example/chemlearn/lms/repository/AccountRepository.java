package com.example.chemlearn.lms.repository;

import com.example.chemlearn.core.entity.Account;
import com.example.chemlearn.lms.enums.AccountRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<Account> findByRole(AccountRole role);
    Optional<Account> findByIdAndRole(UUID id, AccountRole role);
}
