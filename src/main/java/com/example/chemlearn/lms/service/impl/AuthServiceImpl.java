package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.lms.dto.core.auth.AuthResponseDTO;
import com.example.chemlearn.lms.dto.core.auth.LoginRequestDTO;
import com.example.chemlearn.lms.dto.core.auth.RegisterRequestDTO;
import com.example.chemlearn.lms.repository.StudentRepository;
import com.example.chemlearn.lms.repository.UserRepository;
import com.example.chemlearn.lms.service.AuthService;
import com.example.chemlearn.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;

import static com.example.chemlearn.util.PasswordUtil.hash;
import static com.example.chemlearn.util.PasswordUtil.matches;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final StudentRepository studentRepository;
    private final UserRepository repo;
    private final JwtUtil jwtUtil;

    @Override
    public void register(RegisterRequestDTO dto) {
        if (repo.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username exists");
        }
        if (repo.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email exists");
        }
        User user = new User();
        Student student = new Student();
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setPassword(hash(dto.getPassword()));
        String fullName = dto.getFullName() == null ? null : dto.getFullName().trim();
        user.setFullName((fullName == null || fullName.isBlank()) ? dto.getUsername() : fullName);
        user.setRole(UserRole.ROLE_STUDENT);

        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.setIsActive(true);
        //TODO: auto assign avatar
        user.setAvatarUrl(null);

        student.setUsers(user);
        student.setGradeLevel(0);
        student.setLastActiveDate(LocalDate.now());

        studentRepository.save(student);
        repo.save(user);
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO dto) {
        User acc = repo.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!matches(dto.getPassword(), acc.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        return new AuthResponseDTO(jwtUtil.generateToken(acc), acc.getId().toString(), acc.getUsername(), acc.getEmail(), acc.getRole().name());
    }

    @Override
    public void logout(String token) {
        // Stateless JWT logout is handled client-side.
    }
}
