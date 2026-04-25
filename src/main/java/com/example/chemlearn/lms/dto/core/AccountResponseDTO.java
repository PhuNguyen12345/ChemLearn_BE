package com.example.chemlearn.lms.dto.core;

import com.example.chemlearn.core.entity.Account;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
public class AccountResponseDTO {
    private UUID id;
    private String username;
    private String email;
    private String role;
    private boolean enabled;

    //destructor
    public AccountResponseDTO(Account acc) {
        this.id = acc.getId();
        this.username = acc.getUsername();
        this.email = acc.getEmail();
        this.role = acc.getRole().name();
        this.enabled = acc.isEnabled();
    }
}


