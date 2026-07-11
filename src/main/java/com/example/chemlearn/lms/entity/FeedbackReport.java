package com.example.chemlearn.lms.entity;

import com.example.chemlearn.core.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "feedback_reports")
public class FeedbackReport {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @Column(name = "type", nullable = false, length = 30)
    private String type;

    @Column(name = "priority", nullable = false, length = 30)
    private String priority;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, length = Integer.MAX_VALUE)
    private String message;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "admin_note", length = Integer.MAX_VALUE)
    private String adminNote;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    private void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (status == null || status.isBlank()) status = "OPEN";
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = Instant.now();
    }
}
