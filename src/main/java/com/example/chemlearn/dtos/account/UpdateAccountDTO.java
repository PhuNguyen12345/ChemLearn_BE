package com.example.chemlearn.dtos.account;

import com.example.chemlearn.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAccountDTO {
    @Size(min = 4, max = 30)
    private String username;

    @Email
    private String email;

    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,32}$",
            message = "Password must be 8-32 chars, include upper, lower, number and special char"
    )
    private String password;

    private Boolean enabled;
    private Role role;
}
