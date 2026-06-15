package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.core.entity.Parent;
import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.core.entity.Teacher;
import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.core.enums.AuthProvider;
import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.lms.dto.core.AccountResponseDTO;
import com.example.chemlearn.lms.dto.core.CreateAccountDTO;
import com.example.chemlearn.lms.dto.core.UpdateAccountDTO;
import com.example.chemlearn.lms.exception.CustomExceptions;
import com.example.chemlearn.lms.repository.ParentRepository;
import com.example.chemlearn.lms.repository.StudentRepository;
import com.example.chemlearn.lms.repository.TeacherRepository;
import com.example.chemlearn.lms.repository.UserRepository;
import com.example.chemlearn.lms.service.AccountService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.chemlearn.util.PasswordUtil.hash;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ParentRepository  parentRepository;
    private final UserRepository repo;

    @Override
    public List<AccountResponseDTO> findAll() {
        return repo.findAll().stream().map(AccountResponseDTO::new).toList();
    }

    @Override
    public AccountResponseDTO findById(UUID id) {
        User user = repo.findById(id)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Account not found"));
        return new AccountResponseDTO(user);
    }

    @Override
    public AccountResponseDTO create(CreateAccountDTO dto) {
        if (repo.existsByUsername(dto.getUsername())) {
            throw new CustomExceptions.BadRequestException("Username already exists");
        }
        if (repo.existsByEmail(dto.getEmail())) {
            throw new CustomExceptions.BadRequestException("Email already exists");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setPassword(hash(dto.getPassword()));

        //TODO: auto assign a pfp for newly created user
        user.setAvatarUrl(null);

        user.setRole(dto.getRole() != null ? dto.getRole() : UserRole.ROLE_STUDENT);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(null);
        user.setIsActive(true);
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setFailedLoginAttempts(0);
        user.setLockoutUntil(null);

        User savedUser = repo.save(user);

        if(user.getRole().equals(UserRole.ROLE_STUDENT)){
            Student student = new Student();
            student.setUsers(savedUser);
            student.setGradeLevel(0);
            student.setLastActiveDate(LocalDate.now());

            studentRepository.save(student);
        }
        if(user.getRole().equals(UserRole.ROLE_TEACHER)){
            Teacher teacher = new Teacher();
            teacher.setUsers(savedUser);

            teacherRepository.save(teacher);
        }
        if(user.getRole().equals(UserRole.ROLE_PARENT)){
            Parent parent = new Parent();
            parent.setUsers(savedUser);

            parentRepository.save(parent);
        }

        return new AccountResponseDTO(savedUser);
    }

    @Override
    public AccountResponseDTO update(UUID id, UpdateAccountDTO dto) {
        User user = repo.findById(id)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Account not found"));
        if (dto.getUsername() != null) user.setUsername(dto.getUsername());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPassword() != null) user.setPassword(hash(dto.getPassword()));
        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getEnabled() != null) user.setIsActive(dto.getEnabled());
        if (dto.getAvatarUrl() != null) user.setAvatarUrl(dto.getAvatarUrl());
        if (dto.getRole() != null) user.setRole(dto.getRole());
        user.setUpdatedAt(Instant.now());
        return new AccountResponseDTO(repo.save(user));
    }

    @Override
    public AccountResponseDTO deactivate(UUID id) {
        User user = repo.findById(id)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Account not found"));
        user.setIsActive(false);
        user.setUpdatedAt(Instant.now());
        return new AccountResponseDTO(repo.save(user));
    }

    @Override
    public void delete(UUID id) {
        if (!repo.existsById(id)) {
            throw new CustomExceptions.ResourceNotFoundException("Account not found");
        }
        repo.deleteById(id);
    }
}
