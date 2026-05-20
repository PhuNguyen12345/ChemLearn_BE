package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.core.entity.Parent;
import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.lms.repository.StudentRepository;
import com.example.chemlearn.lms.repository.UserRepository;
import com.example.chemlearn.lms.dto.student.ChangePasswordDTO;
import com.example.chemlearn.lms.dto.student.EnrolledClassDTO;
import com.example.chemlearn.lms.dto.student.StudentProfileDTO;
import com.example.chemlearn.lms.dto.student.UpdateProfileDTO;
import com.example.chemlearn.lms.entity.StudyClassEnrollment;
import com.example.chemlearn.lms.repository.StudyClassEnrollmentRepository;
import com.example.chemlearn.lms.service.StudentProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentProfileServiceImpl implements StudentProfileService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final StudyClassEnrollmentRepository enrollmentRepository;
    private final PasswordEncoder passwordEncoder;

    private UUID getAuthenticatedStudentId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @Override
    public StudentProfileDTO getProfile() {
        UUID studentId = getAuthenticatedStudentId();
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));
        
        User user = student.getUsers();
        Parent parent = student.getParent();

        StudentProfileDTO dto = new StudentProfileDTO();
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setGender(user.getGender());
        dto.setGradeLevel(student.getGradeLevel());
        dto.setJoinedAt(user.getCreatedAt());

        if (parent != null) {
            dto.setParentName(parent.getUsers().getFullName());
            dto.setParentContact(parent.getPhoneNumber() != null ? parent.getPhoneNumber() : parent.getUsers().getEmail());
        }

        List<StudyClassEnrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        List<EnrolledClassDTO> classDTOs = enrollments.stream().map(e -> {
            EnrolledClassDTO classDto = new EnrolledClassDTO();
            classDto.setClassId(e.getStudyClassField().getId());
            classDto.setClassName(e.getStudyClassField().getName());
            classDto.setTeacherName(e.getStudyClassField().getTeacher() != null ? e.getStudyClassField().getTeacher().getUsers().getFullName() : "N/A");
            classDto.setProgress(0); // TODO: Calculate actual progress if needed
            return classDto;
        }).collect(Collectors.toList());

        dto.setEnrolledClasses(classDTOs);

        return dto;
    }

    @Override
    public StudentProfileDTO updateProfile(UpdateProfileDTO dto) {
        UUID studentId = getAuthenticatedStudentId();
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setGender(dto.getGender());
        
        userRepository.save(user);

        return getProfile();
    }

    @Override
    public void changePassword(ChangePasswordDTO dto) {
        UUID studentId = getAuthenticatedStudentId();
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }
}
