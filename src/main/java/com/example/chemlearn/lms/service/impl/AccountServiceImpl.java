package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.core.entity.Account;
import com.example.chemlearn.lms.dto.core.AccountResponseDTO;
import com.example.chemlearn.lms.dto.core.CreateAccountDTO;
import com.example.chemlearn.lms.dto.core.UpdateAccountDTO;
import com.example.chemlearn.lms.enums.AccountRole;
import com.example.chemlearn.lms.exception.CustomExceptions;
import com.example.chemlearn.lms.repository.AccountRepository;
import com.example.chemlearn.lms.service.AccountService;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.chemlearn.util.PasswordUtil.hash;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository repo;

    @Override
    public List<AccountResponseDTO> findAll() {
        return repo.findAll().stream().map(AccountResponseDTO::new).toList();
    }

    @Override
    public AccountResponseDTO findById(UUID id) {
        Account acc = repo.findById(id)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Account not found"));
        return new AccountResponseDTO(acc);
    }

    @Override
    public AccountResponseDTO create(CreateAccountDTO dto) {
        if (repo.existsByUsername(dto.getUsername())) {
            throw new CustomExceptions.BadRequestException("Username already exists");
        }
        Account acc = new Account();
        acc.setUsername(dto.getUsername());
        acc.setEmail(dto.getEmail());
        acc.setPassword(hash(dto.getPassword()));
        acc.setRole(dto.getRole() != null ? dto.getRole() : AccountRole.ROLE_STUDENT);
        acc.setEnabled(true);
        return new AccountResponseDTO(repo.save(acc));
    }

    @Override
    public AccountResponseDTO update(UUID id, UpdateAccountDTO dto) {
        Account acc = repo.findById(id)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Account not found"));
        if (dto.getUsername() != null) acc.setUsername(dto.getUsername());
        if (dto.getEmail() != null) acc.setEmail(dto.getEmail());
        if (dto.getPassword() != null) acc.setPassword(hash(dto.getPassword()));
        if (dto.getEnabled() != null) acc.setEnabled(dto.getEnabled());
        if (dto.getRole() != null) acc.setRole(dto.getRole());
        return new AccountResponseDTO(repo.save(acc));
    }

    @Override
    public void delete(UUID id) {
        if (!repo.existsById(id)) {
            throw new CustomExceptions.ResourceNotFoundException("Account not found");
        }
        repo.deleteById(id);
    }
}
