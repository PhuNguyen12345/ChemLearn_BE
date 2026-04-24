package com.example.chemlearn.dtos.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TeacherClassInfoDTO {
    private Long id;
    private String name;
    private String schedule;
    private String description;
    private List<StudentBrief> students;

    @Data
    @AllArgsConstructor
    public static class StudentBrief {
        private Long id;
        private String username;
        private String email;
        private String accountLink;
    }
}
