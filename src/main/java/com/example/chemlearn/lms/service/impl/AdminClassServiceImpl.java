package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.core.entity.Teacher;
import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.lms.dto.admin.AdminClassRequestDTO;
import com.example.chemlearn.lms.dto.admin.AdminClassResponseDTO;
import com.example.chemlearn.lms.entity.ClassStudentLink;
import com.example.chemlearn.lms.entity.StudyClass;
import com.example.chemlearn.lms.repository.ClassStudentLinkRepository;
import com.example.chemlearn.lms.repository.StudyClassRepository;
import com.example.chemlearn.lms.repository.UserRepository;
import com.example.chemlearn.lms.service.AdminClassService;
import com.example.chemlearn.lms.service.StudyClassCodeGenerator;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminClassServiceImpl implements AdminClassService {
    private final StudyClassRepository studyClassRepository;
    private final ClassStudentLinkRepository classStudentLinkRepository;
    private final UserRepository userRepository;
    private final StudyClassCodeGenerator studyClassCodeGenerator;
    private final EntityManager entityManager;

    @Override
    public List<AdminClassResponseDTO> getClasses() {
        return studyClassRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AdminClassResponseDTO createClass(AdminClassRequestDTO dto) {
        StudyClass studyClass = new StudyClass();
        applyClassFields(studyClass, dto);
        studyClass.setClassCode(studyClassCodeGenerator.generateUniqueCode());
        StudyClass saved = studyClassRepository.save(studyClass);
        syncStudents(saved, dto.getStudentIds());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AdminClassResponseDTO updateClass(UUID classId, AdminClassRequestDTO dto) {
        StudyClass studyClass = studyClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        applyClassFields(studyClass, dto);
        StudyClass saved = studyClassRepository.save(studyClass);
        syncStudents(saved, dto.getStudentIds());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteClass(UUID classId) {
        classStudentLinkRepository.deleteByClassRoomId(classId);
        studyClassRepository.deleteById(classId);
    }

    private void applyClassFields(StudyClass studyClass, AdminClassRequestDTO dto) {
        studyClass.setName(dto.getName());
        studyClass.setSchedule(dto.getSchedule());
        studyClass.setDescription(dto.getDescription());
        if (studyClass.getGradeLevel() == null) {
            studyClass.setGradeLevel(10);
        }

        if (dto.getTeacherId() == null) {
            studyClass.setTeacher(null);
        } else {
            User teacherUser = userRepository.findByIdAndRole(dto.getTeacherId(), UserRole.ROLE_TEACHER)
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));
            studyClass.setTeacher(entityManager.getReference(Teacher.class, teacherUser.getId()));
        }

        Instant now = Instant.now();
        if (studyClass.getCreatedAt() == null) {
            studyClass.setCreatedAt(now);
        }
        studyClass.setUpdatedAt(now);
    }

    private void syncStudents(StudyClass studyClass, List<UUID> studentIds) {
        classStudentLinkRepository.deleteByClassRoomId(studyClass.getId());
        if (studentIds == null || studentIds.isEmpty()) {
            return;
        }

        List<ClassStudentLink> links = new ArrayList<>();
        for (UUID studentId : new LinkedHashSet<>(studentIds)) {
            User student = userRepository.findByIdAndRole(studentId, UserRole.ROLE_STUDENT)
                    .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
            ClassStudentLink link = new ClassStudentLink();
            link.setClassRoom(studyClass);
            link.setStudent(student);
            links.add(link);
        }
        classStudentLinkRepository.saveAll(links);
    }

    private AdminClassResponseDTO toResponse(StudyClass studyClass) {
        AdminClassResponseDTO.TeacherBrief teacherBrief = null;
        if (studyClass.getTeacher() != null && studyClass.getTeacher().getUsers() != null) {
            teacherBrief = new AdminClassResponseDTO.TeacherBrief(
                    studyClass.getTeacher().getId(),
                    studyClass.getTeacher().getUsers().getUsername(),
                    studyClass.getTeacher().getUsers().getEmail());
        }

        List<AdminClassResponseDTO.StudentBrief> students = classStudentLinkRepository
                .findByClassRoomIdOrderByStudentUsernameAsc(studyClass.getId())
                .stream()
                .map(link -> new AdminClassResponseDTO.StudentBrief(
                        link.getStudent().getId(),
                        link.getStudent().getUsername(),
                        link.getStudent().getEmail()))
                .toList();

        return new AdminClassResponseDTO(
                studyClass.getId(),
                studyClass.getName(),
            studyClass.getSchedule(),
            studyClass.getDescription(),
                studyClass.getClassCode(),
                teacherBrief,
                students);
    }
}
