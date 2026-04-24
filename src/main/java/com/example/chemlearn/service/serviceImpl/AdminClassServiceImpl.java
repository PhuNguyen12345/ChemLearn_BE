package com.example.chemlearn.service.serviceImpl;

import com.example.chemlearn.dtos.admin.AdminClassRequestDTO;
import com.example.chemlearn.dtos.admin.AdminClassResponseDTO;
import com.example.chemlearn.entity.Account;
import com.example.chemlearn.entity.ChemClass;
import com.example.chemlearn.entity.ClassStudentLink;
import com.example.chemlearn.enums.AccountRole;
import com.example.chemlearn.exception.CustomExceptions;
import com.example.chemlearn.repository.AccountRepository;
import com.example.chemlearn.repository.ChemClassRepository;
import com.example.chemlearn.repository.ClassStudentLinkRepository;
import com.example.chemlearn.service.AdminClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminClassServiceImpl implements AdminClassService {

    private final ChemClassRepository chemClassRepository;
    private final ClassStudentLinkRepository classStudentLinkRepository;
    private final AccountRepository accountRepository;

    @Override
    public List<AdminClassResponseDTO> getClasses() {
        return chemClassRepository.findAll().stream()
                .sorted(Comparator.comparing(ChemClass::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public AdminClassResponseDTO createClass(AdminClassRequestDTO dto) {
        ChemClass classRoom = new ChemClass();
        applyBaseFields(classRoom, dto);
        chemClassRepository.save(classRoom);

        replaceStudents(classRoom, dto.getStudentIds());
        return toDto(classRoom);
    }

    @Override
    @Transactional
    public AdminClassResponseDTO updateClass(Long classId, AdminClassRequestDTO dto) {
        ChemClass classRoom = chemClassRepository.findById(classId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Class not found"));

        applyBaseFields(classRoom, dto);
        chemClassRepository.save(classRoom);

        replaceStudents(classRoom, dto.getStudentIds());
        return toDto(classRoom);
    }

    @Override
    @Transactional
    public void deleteClass(Long classId) {
        ChemClass classRoom = chemClassRepository.findById(classId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Class not found"));

        classStudentLinkRepository.deleteByClassRoomId(classRoom.getId());
        chemClassRepository.delete(classRoom);
    }

    private void applyBaseFields(ChemClass classRoom, AdminClassRequestDTO dto) {
        classRoom.setName(dto.getName());
        classRoom.setSchedule(dto.getSchedule());
        classRoom.setDescription(dto.getDescription());

        if (dto.getTeacherId() == null) {
            classRoom.setTeacher(null);
            return;
        }

        Account teacher = accountRepository.findByIdAndRole(dto.getTeacherId(), AccountRole.ROLE_TEACHER)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Teacher not found"));

        classRoom.setTeacher(teacher);
    }

    private void replaceStudents(ChemClass classRoom, List<Long> studentIds) {
        classStudentLinkRepository.deleteByClassRoomId(classRoom.getId());

        if (studentIds == null || studentIds.isEmpty()) {
            return;
        }

        Set<Long> uniqueStudentIds = new HashSet<>(studentIds);
        List<ClassStudentLink> links = new ArrayList<>();
        for (Long studentId : uniqueStudentIds) {
            Account student = accountRepository.findByIdAndRole(studentId, AccountRole.ROLE_STUDENT)
                    .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Student not found: " + studentId));

            ClassStudentLink link = new ClassStudentLink();
            link.setClassRoom(classRoom);
            link.setStudent(student);
            links.add(link);
        }

        classStudentLinkRepository.saveAll(links);
    }

    private AdminClassResponseDTO toDto(ChemClass classRoom) {
        Account teacher = classRoom.getTeacher();
        AdminClassResponseDTO.TeacherBrief teacherDto = teacher == null
                ? null
                : new AdminClassResponseDTO.TeacherBrief(teacher.getId(), teacher.getUsername(), teacher.getEmail());

        List<AdminClassResponseDTO.StudentBrief> studentDtos = classStudentLinkRepository
                .findByClassRoomIdOrderByStudentUsernameAsc(classRoom.getId())
                .stream()
                .map(link -> new AdminClassResponseDTO.StudentBrief(
                        link.getStudent().getId(),
                        link.getStudent().getUsername(),
                        link.getStudent().getEmail()
                ))
                .toList();

        return new AdminClassResponseDTO(
                classRoom.getId(),
                classRoom.getName(),
                classRoom.getSchedule(),
                classRoom.getDescription(),
                teacherDto,
                studentDtos
        );
    }
}
