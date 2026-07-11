package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.core.entity.Parent;
import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.core.entity.Teacher;
import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.core.util.GradeCalculator;
import com.example.chemlearn.core.enums.AuthProvider;
import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.lms.dto.core.AccountResponseDTO;
import com.example.chemlearn.lms.dto.core.CreateAccountDTO;
import com.example.chemlearn.lms.dto.core.UpdateAccountDTO;
import com.example.chemlearn.lms.dto.core.UpdateGraduationYearDTO;
import com.example.chemlearn.lms.exception.CustomExceptions;
import com.example.chemlearn.lms.repository.ParentRepository;
import com.example.chemlearn.lms.repository.StudentRepository;
import com.example.chemlearn.lms.repository.TeacherRepository;
import com.example.chemlearn.lms.repository.UserRepository;
import com.example.chemlearn.lms.service.AccountService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
        List<User> users = repo.findAll();
        List<UUID> userIds = users.stream().map(User::getId).toList();

        Map<UUID, Student> studentMap = studentRepository.findAllById(userIds)
                .stream().collect(Collectors.toMap(Student::getId, s -> s));
        Map<UUID, Teacher> teacherMap = teacherRepository.findAllById(userIds)
                .stream().collect(Collectors.toMap(Teacher::getId, t -> t));

        return users.stream().map(u -> new AccountResponseDTO(u, studentMap.get(u.getId()), teacherMap.get(u.getId()))).toList();
    }

    @Override
    public AccountResponseDTO findById(UUID id) {
        User user = repo.findById(id)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Account not found"));
        Student student = user.getRole() == UserRole.ROLE_STUDENT ? studentRepository.findById(id).orElse(null) : null;
        Teacher teacher = user.getRole() == UserRole.ROLE_TEACHER ? teacherRepository.findById(id).orElse(null) : null;
        return new AccountResponseDTO(user, student, teacher);
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
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setGender(dto.getGender());

        user.setRole(dto.getRole() != null ? dto.getRole() : UserRole.ROLE_STUDENT);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(null);
        user.setIsActive(true);
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setFailedLoginAttempts(0);
        user.setLockoutUntil(null);

        User savedUser = repo.save(user);
        Student savedStudent = null;
        Teacher savedTeacher = null;

        if(savedUser.getRole().equals(UserRole.ROLE_STUDENT)){
            Student student = new Student();
            student.setUsers(savedUser);
            // Fix: grade 0 violated CHECK constraint. Default to MIN_GRADE (6).
            int defaultGrade = dto.getGradeLevel() != null ? dto.getGradeLevel() : GradeCalculator.MIN_GRADE;
            student.setGradeLevel(defaultGrade);                      // Dual-write: backward compat
            student.setTargetGraduationYear(                          // Dual-write: new dynamic logic
                    GradeCalculator.calculateTargetGraduationYear(defaultGrade)
            );
            student.setSchoolName(dto.getSchoolName());
            student.setLastActiveDate(LocalDate.now());

            savedStudent = studentRepository.save(student);
        } else if(savedUser.getRole().equals(UserRole.ROLE_TEACHER)){
            Teacher teacher = new Teacher();
            teacher.setUsers(savedUser);
            teacher.setBio(dto.getBio());
            teacher.setSpecialization(dto.getSpecialization());
            teacher.setDegree(dto.getDegree());
            teacher.setWorkplace(dto.getWorkplace());

            savedTeacher = teacherRepository.save(teacher);
        } else if(savedUser.getRole().equals(UserRole.ROLE_PARENT)){
            Parent parent = new Parent();
            parent.setUsers(savedUser);

            parentRepository.save(parent);
        }

        return new AccountResponseDTO(savedUser, savedStudent, savedTeacher);
    }

    @Override
    public AccountResponseDTO update(UUID id, UpdateAccountDTO dto) {
        User user = repo.findById(id)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Account not found"));
        if (dto.getUsername() != null) user.setUsername(dto.getUsername());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) user.setPassword(hash(dto.getPassword()));
        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getEnabled() != null) user.setIsActive(dto.getEnabled());
        if (dto.getAvatarUrl() != null) user.setAvatarUrl(dto.getAvatarUrl());
        if (dto.getRole() != null) user.setRole(dto.getRole());
        if (dto.getPhoneNumber() != null) user.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        user.setUpdatedAt(Instant.now());
        User savedUser = repo.save(user);

        Student student = null;
        Teacher teacher = null;

        if (savedUser.getRole() == UserRole.ROLE_STUDENT) {
            student = studentRepository.findById(id).orElse(new Student());
            if (student.getUsers() == null) student.setUsers(savedUser);
            if (student.getLastActiveDate() == null) student.setLastActiveDate(LocalDate.now());
            
            if (dto.getGradeLevel() != null) {
                student.setGradeLevel(dto.getGradeLevel());
                student.setTargetGraduationYear(GradeCalculator.calculateTargetGraduationYear(dto.getGradeLevel()));
            }
            if (dto.getSchoolName() != null) {
                student.setSchoolName(dto.getSchoolName());
            }
            student = studentRepository.save(student);
        } else if (savedUser.getRole() == UserRole.ROLE_TEACHER) {
            teacher = teacherRepository.findById(id).orElse(new Teacher());
            if (teacher.getUsers() == null) teacher.setUsers(savedUser);

            if (dto.getBio() != null) teacher.setBio(dto.getBio());
            if (dto.getSpecialization() != null) teacher.setSpecialization(dto.getSpecialization());
            if (dto.getDegree() != null) teacher.setDegree(dto.getDegree());
            if (dto.getWorkplace() != null) teacher.setWorkplace(dto.getWorkplace());
            
            teacher = teacherRepository.save(teacher);
        }

        return new AccountResponseDTO(savedUser, student, teacher);
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

    /**
     * Admin override: cập nhật target_graduation_year cho học sinh.
     * Đồng thời cập nhật grade_level (dual-write) để giữ tương thích ngược.
     */
    @Override
    public void updateGraduationYear(UUID studentUserId, UpdateGraduationYearDTO dto) {
        Student student = studentRepository.findById(studentUserId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException(
                        "Student not found with id: " + studentUserId));

        int newTargetYear = dto.getTargetGraduationYear();
        student.setTargetGraduationYear(newTargetYear);

        // Dual-write: cập nhật grade_level cho tương thích ngược
        int currentGrade = GradeCalculator.calculateCurrentGrade(newTargetYear);
        // Clamp grade_level trong phạm vi hợp lệ cho DB CHECK constraint
        int clampedGrade = Math.max(GradeCalculator.MIN_GRADE,
                Math.min(currentGrade, GradeCalculator.MAX_GRADE));
        student.setGradeLevel(clampedGrade);

        studentRepository.save(student);
    }
}
