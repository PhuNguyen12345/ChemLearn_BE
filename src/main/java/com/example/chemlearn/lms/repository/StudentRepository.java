package com.example.chemlearn.lms.repository;

import com.example.chemlearn.core.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {

}
