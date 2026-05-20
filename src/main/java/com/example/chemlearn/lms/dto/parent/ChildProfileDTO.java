package com.example.chemlearn.lms.dto.parent;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ChildProfileDTO {
    private UUID studentId;
    private String fullName;
    private String email;
    private String schoolName;
    private Integer gradeLevel;
    private String avatarUrl;
}
