package com.example.chemlearn.repository;

import com.example.chemlearn.entity.ChemClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChemClassRepository extends JpaRepository<ChemClass, Long> {
    List<ChemClass> findByTeacherIdOrderByNameAsc(Long teacherId);
}
