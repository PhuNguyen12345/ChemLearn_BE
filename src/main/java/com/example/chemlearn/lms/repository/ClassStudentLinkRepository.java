package com.example.chemlearn.lms.repository;

import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.lms.entity.ClassStudentLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
public interface ClassStudentLinkRepository extends JpaRepository<ClassStudentLink, UUID> {
    List<ClassStudentLink> findByClassRoomIdOrderByStudentUsernameAsc(UUID classId);
    List<ClassStudentLink> findByClassRoomTeacherIdOrderByClassRoomNameAsc(UUID teacherId);
    List<ClassStudentLink> findByStudentIdAndClassRoomTeacherId(UUID studentId, UUID teacherId);
    Optional<ClassStudentLink> findByStudentIdAndClassRoomId(UUID studentId, UUID classId);
    boolean existsByStudentIdAndClassRoomTeacherId(UUID studentId, UUID teacherId);
    void deleteByClassRoomId(UUID classId);
    void deleteByStudentIdAndClassRoomId(UUID studentId, UUID classId);
    List<ClassStudentLink> findByStudentId(UUID studentId);

    @Query("""
            select link.student
            from ClassStudentLink link
            where link.classRoom.id = :classId
              and link.student.isActive = true
            order by link.student.fullName asc
            """)
    List<User> findActiveStudentsByClassRoomId(@Param("classId") UUID classId);
}
