package com.example.chemlearn.lms.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AdminClassRequestDTO {
    @NotBlank
    private String name;

    private String schedule;

    private String description;

    private UUID teacherId;

    private List<UUID> studentIds;
}

