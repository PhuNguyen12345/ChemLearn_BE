package com.example.chemlearn.dtos.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TeacherStudentAccountDTO {
    private Long id;
    private String username;
    private String email;
    private boolean enabled;
    private List<String> classes;
}
