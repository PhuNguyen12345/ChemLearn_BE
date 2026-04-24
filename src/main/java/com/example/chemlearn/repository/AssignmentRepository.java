package com.example.chemlearn.repository;

import com.example.chemlearn.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
	List<Assignment> findByTeacherIdOrderByIdDesc(Long teacherId);

	List<Assignment> findByStudentIdOrderByIdDesc(Long studentId);
}
