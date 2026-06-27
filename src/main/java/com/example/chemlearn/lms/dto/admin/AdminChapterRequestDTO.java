package com.example.chemlearn.lms.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminChapterRequestDTO {
    @NotBlank(message = "Chapter title is required")
    private String title;

    private String description;

    private Integer orderIndex;

    private Boolean published = true;

    private Integer gradeLevel;

    private Boolean needPurchase;
}
