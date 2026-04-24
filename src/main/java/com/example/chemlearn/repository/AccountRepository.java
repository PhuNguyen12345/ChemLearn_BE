package com.example.chemlearn.repository;

import com.example.chemlearn.entity.Account;
import com.example.chemlearn.enums.AccountRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {
    Optional<Account> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<Account> findByRole(AccountRole role);

    Optional<Account> findByIdAndRole(Long id, AccountRole role);
}
