package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.dto.core.AccountResponseDTO;
import com.example.chemlearn.lms.dto.core.CreateAccountDTO;
import com.example.chemlearn.lms.dto.core.UpdateAccountDTO;
import com.example.chemlearn.lms.dto.core.UpdateGraduationYearDTO;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    List<AccountResponseDTO> findAll();

    AccountResponseDTO findById(UUID id);

    AccountResponseDTO create(CreateAccountDTO dto);

    AccountResponseDTO update(UUID id, UpdateAccountDTO dto);

    AccountResponseDTO deactivate(UUID id);

    void delete(UUID id);

    /**
     * Admin override: cập nhật target_graduation_year cho học sinh.
     * Dùng cho trường hợp đặc biệt: lưu ban, chuyển trường, điều chỉnh thủ công.
     *
     * @param studentUserId UUID của user (student)
     * @param dto chứa targetGraduationYear mới
     */
    void updateGraduationYear(UUID studentUserId, UpdateGraduationYearDTO dto);
}

