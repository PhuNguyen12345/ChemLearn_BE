package com.example.chemlearn.lms.dto.core;

import com.example.chemlearn.core.entity.User;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class AccountResponseDTO {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private String avatarUrl;
    private Boolean isActive;
    private Instant created;
    private Instant updated;

    //destructor
    public AccountResponseDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.role = user.getRole().name();
        this.avatarUrl = user.getAvatarUrl();
        this.created = user.getCreatedAt();
        this.updated = user.getUpdatedAt();
        this.isActive = user.getIsActive();
    }
}


