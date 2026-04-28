package com.example.chemlearn.lms.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class AdminClassResponseDTO {
    private UUID id;
    private String name;
    private String schedule;
    private String description;
    private TeacherBrief teacher;
    private List<StudentBrief> students;

    @Data
    @AllArgsConstructor
    public static class TeacherBrief {
        private UUID id;
        private String username;
        private String email;
    }

    @Data
    @AllArgsConstructor
    public static class StudentBrief {
        private UUID id;
        private String username;
        private String email;
    }
}

