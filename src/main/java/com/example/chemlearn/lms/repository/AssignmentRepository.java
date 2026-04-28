package com.example.chemlearn.lms.repository;

import com.example.chemlearn.lms.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {
	List<Assignment> findByTeacherIdOrderByIdDesc(UUID teacherId);
	List<Assignment> findByStudentIdOrderByIdDesc(UUID studentId);
}
