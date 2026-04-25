package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.lms.dto.admin.AdminClassRequestDTO;
import com.example.chemlearn.lms.dto.admin.AdminClassResponseDTO;
import com.example.chemlearn.lms.service.AdminClassService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminClassServiceImpl implements AdminClassService {
    @Override
    public List<AdminClassResponseDTO> getClasses() {
        return List.of();
    }

    @Override
    public AdminClassResponseDTO createClass(AdminClassRequestDTO dto) {
        return null;
    }

    @Override
    public AdminClassResponseDTO updateClass(Long classId, AdminClassRequestDTO dto) {
        return null;
    }

    @Override
    public void deleteClass(Long classId) {

    }
}
