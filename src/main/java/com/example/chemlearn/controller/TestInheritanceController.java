package com.example.chemlearn.controller;

import com.example.chemlearn.entity.Teacher;
import com.example.chemlearn.entity.User;
import com.example.chemlearn.repository.TeacherRepository;
import com.example.chemlearn.repository.UserRepository;
import com.example.chemlearn.service.TeacherService;
import org.aspectj.weaver.ast.Test;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestInheritanceController {
    private UserRepository userRepository;
    private TeacherRepository teacherRepository;
    private TeacherService teacherService;

    public TestInheritanceController(UserRepository userRepository, TeacherRepository teacherRepository,  TeacherService teacherService) {
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
        this.teacherService = teacherService;
    }

    // API 1: Tạo thử một Teacher
    @PostMapping("/create-teacher")
    public Teacher createTeacher(@RequestBody Teacher teacher) {
        // Lưu ý: Bạn chỉ cần save vào teacherRepository
        // JPA sẽ tự động cắt dữ liệu: phần chung vào bảng users, phần riêng vào bảng teachers
        return teacherService.createTeacher(teacher);
    }

    // API 2: Lấy thông tin từ UserRepository (Test tính đa hình)
    @GetMapping("/get-user/{id}")
    public User getUser(@PathVariable Long id) {
        // Bạn tìm trong bảng User, nhưng nếu ID đó là Teacher,
        // nó sẽ trả về đầy đủ thông tin cả bằng cấp (Degree) luôn!
        return userRepository.findById(id).orElse(null);
    }

}
