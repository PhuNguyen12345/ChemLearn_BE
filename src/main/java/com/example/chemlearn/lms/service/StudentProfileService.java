package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.dto.student.ChangePasswordDTO;
import com.example.chemlearn.lms.dto.student.StudentProfileDTO;
import com.example.chemlearn.lms.dto.student.UpdateProfileDTO;

public interface StudentProfileService {
    StudentProfileDTO getProfile();
    StudentProfileDTO updateProfile(UpdateProfileDTO dto);
    void changePassword(ChangePasswordDTO dto);
}
