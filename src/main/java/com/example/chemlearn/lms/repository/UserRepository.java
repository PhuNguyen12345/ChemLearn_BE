package com.example.chemlearn.lms.repository;

import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.core.enums.UserRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderSubject(String providerSubject);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
    Optional<User> findByIdAndRole(UUID id, UserRole role);
}
