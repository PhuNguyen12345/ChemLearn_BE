package com.example.chemlearn.lms.dto.core.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDTO {
    @NotBlank
    @Size(min = 4, max = 30)
    @Pattern(regexp = "^\\S+$", message = "Username must not contain whitespace")
    private String username;

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    @Size(min = 3, max = 100)
    @NotBlank
    @Pattern(regexp = "^\\p{L}+(?: \\p{L}+)*$", message = "Full name must not contain special characters")
    private String fullName;

    public void setFullName(String fullName) {
        this.fullName = fullName == null ? null : fullName.trim();
    }

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_.]).{8,32}$",
            message = "Password must be 8-32 chars, include upper, lower, number and special char"
    )
    private String password;

        @Min(value = 6, message = "Grade level must be between 6 and 12")
        @Max(value = 12, message = "Grade level must be between 6 and 12")
        private Integer gradeLevel;

        private String gender;

    // Role selection: "ROLE_STUDENT", "ROLE_TEACHER", "ROLE_PARENT"
    private String role;

    // Common for teacher & parent
    private String phoneNumber;

    // Teacher-specific
    private String degree;          // "Cử nhân", "Thạc sĩ", "Tiến sĩ"
    private String specialization;
    private String workplace;

    // Parent-specific
    private String jobTitle;
}
