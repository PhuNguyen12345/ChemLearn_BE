package com.example.chemlearn.dtos;

import com.example.chemlearn.entity.Account;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class AccountResponseDTO {
    private Long id;
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
