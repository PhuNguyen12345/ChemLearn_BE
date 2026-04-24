package com.example.chemlearn.service;

import com.example.chemlearn.dtos.admin.AdminClassRequestDTO;
import com.example.chemlearn.dtos.admin.AdminClassResponseDTO;

import java.util.List;

public interface AdminClassService {
    List<AdminClassResponseDTO> getClasses();

    AdminClassResponseDTO createClass(AdminClassRequestDTO dto);

    AdminClassResponseDTO updateClass(Long classId, AdminClassRequestDTO dto);

    void deleteClass(Long classId);
}
