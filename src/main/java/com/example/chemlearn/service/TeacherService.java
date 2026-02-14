package com.example.chemlearn.service;

import com.example.chemlearn.entity.Teacher;

import java.util.Optional;

public interface TeacherService {
    Optional<Teacher> findByUsername(String username);
    Optional<Teacher> findByEmail(String email);
    Optional<Teacher> findByPhone(String phone);
    Teacher createTeacher(Teacher teacher);
}
