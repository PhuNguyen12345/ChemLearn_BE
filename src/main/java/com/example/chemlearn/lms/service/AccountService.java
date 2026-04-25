package com.example.chemlearn.lms.service;

import com.example.chemlearn.lms.dto.core.AccountResponseDTO;
import com.example.chemlearn.lms.dto.core.CreateAccountDTO;
import com.example.chemlearn.lms.dto.core.UpdateAccountDTO;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    List<AccountResponseDTO> findAll();

    AccountResponseDTO findById(UUID id);

    AccountResponseDTO create(CreateAccountDTO dto);

    AccountResponseDTO update(UUID id, UpdateAccountDTO dto);

    void delete(UUID id);
}

