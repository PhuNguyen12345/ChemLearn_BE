package com.example.chemlearn.dtos.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class AdminClassRequestDTO {
    @NotBlank
    private String name;

    private String schedule;

    private String description;

    private Long teacherId;

    private List<Long> studentIds;
}
