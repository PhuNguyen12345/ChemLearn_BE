package com.example.chemlearn.lms.repository;

import com.example.chemlearn.core.entity.Invite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InviteRepository extends JpaRepository<Invite, UUID> {
    Optional<Invite> findByToken(String token);
    Optional<Invite> findByEmail(String email);
    boolean existsByEmail(String email);
}
