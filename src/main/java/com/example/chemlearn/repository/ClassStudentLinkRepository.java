package com.example.chemlearn.repository;

import com.example.chemlearn.entity.ClassStudentLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassStudentLinkRepository extends JpaRepository<ClassStudentLink, Long> {
    List<ClassStudentLink> findByClassRoomIdOrderByStudentUsernameAsc(Long classId);

    List<ClassStudentLink> findByClassRoomTeacherIdOrderByClassRoomNameAsc(Long teacherId);

    List<ClassStudentLink> findByStudentIdAndClassRoomTeacherId(Long studentId, Long teacherId);

    boolean existsByStudentIdAndClassRoomTeacherId(Long studentId, Long teacherId);

    void deleteByClassRoomId(Long classId);
}
