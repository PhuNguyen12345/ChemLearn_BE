package com.example.chemlearn.service.serviceImpl;

import com.example.chemlearn.entity.Teacher;
import com.example.chemlearn.enums.Role;
import com.example.chemlearn.repository.TeacherRepository;
import com.example.chemlearn.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TeacherServiceImpl implements TeacherService {
    private TeacherRepository repository;

    public TeacherServiceImpl(TeacherRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Teacher> findByUsername(String username) {
        return Optional.empty();
    }

    @Override
    public Optional<Teacher> findByEmail(String email) {
        return Optional.empty();
    }

    @Override
    public Optional<Teacher> findByPhone(String phone) {
        return Optional.empty();
    }

    @Override
    public Teacher createTeacher(Teacher teacher) {
        teacher.setRole(Role.TEACHER);
        Teacher savedTeacher = repository.save(teacher);
        savedTeacher.setRole(Role.TEACHER);
        return savedTeacher;
    }
}
