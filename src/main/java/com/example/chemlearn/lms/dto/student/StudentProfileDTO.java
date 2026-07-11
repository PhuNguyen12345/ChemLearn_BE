package com.example.chemlearn.lms.dto.student;

import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
public class StudentProfileDTO {
    // Basic Info
    private String fullName;
    private String email;
    private String phoneNumber;
    private String gender;
    private Integer gradeLevel;
    private Integer targetGraduationYear;
    private String studentStatus; // ACTIVE, GRADUATED, NOT_YET_ENROLLED
    private Instant joinedAt;
    
    // Parent Info
    private String parentName;
    private String parentContact; // Email or phone
    
    // Classes
    private List<EnrolledClassDTO> enrolledClasses;
}
