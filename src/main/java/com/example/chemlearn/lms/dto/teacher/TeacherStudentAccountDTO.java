package com.example.chemlearn.lms.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class TeacherStudentAccountDTO {
    private UUID id;
    private String username;
    private String email;
    private boolean enabled;
    private List<String> classes;
}

