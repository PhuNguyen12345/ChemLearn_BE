package com.example.chemlearn.core.entity;

import com.example.chemlearn.core.enums.AuthProvider;
import com.example.chemlearn.core.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "users_username_key", columnNames = {"username"}),
        @UniqueConstraint(name = "users_email_key", columnNames = {"email"})
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    @Column(name = "avatar_url", length = Integer.MAX_VALUE)
    private String avatarUrl;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "gender", length = 20)
    private String gender;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "is_active")
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    private AuthProvider authProvider;

    @Column(name = "provider_subject", length = 255)
    private String providerSubject;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts;

    @Column(name = "lockout_until")
    private Instant lockoutUntil;

    @Column(name = "last_failed_at")
    private Instant lastFailedAt;

    @PrePersist
    private void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (isActive == null) isActive = true;
        if (fullName == null || fullName.isBlank()) fullName = username;
        if (authProvider == null) authProvider = AuthProvider.LOCAL;
        if (failedLoginAttempts == null) failedLoginAttempts = 0;
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = Instant.now();
        if (isActive == null) isActive = true;
        if (authProvider == null) authProvider = AuthProvider.LOCAL;
    }

}