package com.example.chemlearn.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "invites", uniqueConstraints = {
        @UniqueConstraint(name = "invites_email_key", columnNames = {"email"}),
        @UniqueConstraint(name = "invites_token_key", columnNames = {"token"})
})
public class Invite {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "role", nullable = false, length = 20)
    private String role; // ROLE_TEACHER or ROLE_PARENT

    @Column(name = "token", nullable = false, length = 100)
    private String token;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // PENDING, ACCEPTED, EXPIRED

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    private void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (status == null) status = "PENDING";
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = Instant.now();
    }
}
