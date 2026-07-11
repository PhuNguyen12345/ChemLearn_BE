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
    private Integer targetGraduationYear;
    private String studentStatus; // ACTIVE, GRADUATED, NOT_YET_ENROLLED
    private String avatarUrl;
}
