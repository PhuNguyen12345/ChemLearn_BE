package com.example.chemlearn.service;

import com.example.chemlearn.dtos.AccountResponseDTO;
import com.example.chemlearn.dtos.CreateAccountDTO;
import com.example.chemlearn.dtos.UpdateAccountDTO;
import com.example.chemlearn.entity.Account;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AccountService {

    List<AccountResponseDTO> findAll();

    AccountResponseDTO findById(Long id);

    AccountResponseDTO create(CreateAccountDTO dto);

    AccountResponseDTO update(Long id, UpdateAccountDTO dto);

    void delete(Long id);
}
