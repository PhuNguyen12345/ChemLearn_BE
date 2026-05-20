package com.example.chemlearn.lms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account_link_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountLinkRequest {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID initiatorId;

    @Column(nullable = false)
    private String targetEmail;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String status; // PENDING, ACCEPTED, REJECTED

    @Column(nullable = false)
    private Instant createdAt;
}
