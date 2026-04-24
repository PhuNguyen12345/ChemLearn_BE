package com.example.chemlearn.dtos.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AdminClassResponseDTO {
    private Long id;
    private String name;
    private String schedule;
    private String description;
    private TeacherBrief teacher;
    private List<StudentBrief> students;

    @Data
    @AllArgsConstructor
    public static class TeacherBrief {
        private Long id;
        private String username;
        private String email;
    }

    @Data
    @AllArgsConstructor
    public static class StudentBrief {
        private Long id;
        private String username;
        private String email;
    }
}
