package com.example.chemlearn.lms.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentInClassDTO {
    private UUID studentId;
    private String fullName;
    private String email;
    private String avatarUrl;
    private Integer totalPoints;
    private Integer experience;
    private Integer level;
}
