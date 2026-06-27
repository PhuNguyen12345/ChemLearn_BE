package com.example.chemlearn.lms.dto.admin;

import com.example.chemlearn.core.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MailAnnouncementRequestDTO {
    @NotBlank
    @Size(max = 140)
    private String subject;

    @Size(max = 60)
    private String category;

    @NotBlank
    @Size(max = 160)
    private String title;

    @NotBlank
    @Size(max = 1200)
    private String message;

    private List<@Size(max = 220) String> highlights;

    private List<UserRole> targetRoles;

    @Size(max = 60)
    private String ctaLabel;

    @Size(max = 500)
    private String ctaUrl;
}
