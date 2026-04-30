package com.example.chemlearn.lms.repository;

import com.example.chemlearn.lms.entity.ClassStudentLink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;
public interface ClassStudentLinkRepository extends JpaRepository<ClassStudentLink, UUID> {
    List<ClassStudentLink> findByClassRoomIdOrderByStudentUsernameAsc(UUID classId);
    List<ClassStudentLink> findByClassRoomTeacherIdOrderByClassRoomNameAsc(UUID teacherId);
    List<ClassStudentLink> findByStudentIdAndClassRoomTeacherId(UUID studentId, UUID teacherId);
    boolean existsByStudentIdAndClassRoomTeacherId(UUID studentId, UUID teacherId);
    void deleteByClassRoomId(UUID classId);
    List<ClassStudentLink> findByStudentId(UUID studentId);
}
