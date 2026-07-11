package com.example.chemlearn.lms.dto.parent;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ParentChildDTO {
    private UUID studentId;
    private String username;
    private String fullName;
    private String email;
    private String avatarUrl;
    private String schoolName;
    private Integer gradeLevel;
    private Integer targetGraduationYear;
    private String studentStatus; // ACTIVE, GRADUATED, NOT_YET_ENROLLED
}

