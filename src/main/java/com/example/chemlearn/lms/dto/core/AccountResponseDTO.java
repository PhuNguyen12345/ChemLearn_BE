package com.example.chemlearn.lms.dto.core;

import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.core.entity.Teacher;
import com.example.chemlearn.core.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
public class AccountResponseDTO {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private String avatarUrl;
    private String phoneNumber;
    private String gender;
    private Boolean isActive;
    private Instant created;
    private Instant updated;
    
    // Student fields
    private Integer gradeLevel;
    private Integer targetGraduationYear;
    private String schoolName;

    // Teacher fields
    private String bio;
    private String specialization;
    private String degree;
    private String workplace;

    public AccountResponseDTO(User user, Student student, Teacher teacher) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.role = user.getRole().name();
        this.avatarUrl = user.getAvatarUrl();
        this.phoneNumber = user.getPhoneNumber();
        this.gender = user.getGender();
        this.created = user.getCreatedAt();
        this.updated = user.getUpdatedAt();
        this.isActive = user.getIsActive();

        if (student != null) {
            this.gradeLevel = student.getCurrentGrade();
            this.targetGraduationYear = student.getTargetGraduationYear();
            this.schoolName = student.getSchoolName();
        }

        if (teacher != null) {
            this.bio = teacher.getBio();
            this.specialization = teacher.getSpecialization();
            this.degree = teacher.getDegree();
            this.workplace = teacher.getWorkplace();
        }
    }

    public AccountResponseDTO(User user) {
        this(user, null, null);
    }
}


