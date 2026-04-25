package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.dto.admin.AdminClassRequestDTO;
import com.example.chemlearn.lms.dto.admin.AdminClassResponseDTO;

import java.util.List;

public interface AdminClassService {
    List<AdminClassResponseDTO> getClasses();

    AdminClassResponseDTO createClass(AdminClassRequestDTO dto);

    AdminClassResponseDTO updateClass(Long classId, AdminClassRequestDTO dto);

    void deleteClass(Long classId);
}

