package com.example.chemlearn.service.serviceImpl;

import com.example.chemlearn.dtos.AccountResponseDTO;
import com.example.chemlearn.dtos.CreateAccountDTO;
import com.example.chemlearn.dtos.UpdateAccountDTO;
import com.example.chemlearn.entity.Account;
import com.example.chemlearn.enums.AccountRole;
import com.example.chemlearn.exception.CustomExceptions;
import com.example.chemlearn.repository.AccountRepository;
import com.example.chemlearn.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository repo;
    private final PasswordEncoder encoder;

    @Override
    public List<AccountResponseDTO> findAll() {
        return repo.findAll()
                .stream()
                .map(AccountResponseDTO::new)
                .toList();
    }

    @Override
    public AccountResponseDTO findById(Long id) {
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
        acc.setPassword(encoder.encode(dto.getPassword()));
        acc.setRole(dto.getRole() != null ? dto.getRole() : AccountRole.ROLE_STUDENT);
        acc.setEnabled(true);

        repo.save(acc);
        return new AccountResponseDTO(acc);
    }

    @Override
    public AccountResponseDTO update(Long id, UpdateAccountDTO dto) {

        Account acc = repo.findById(id)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Account not found"));

        if (dto.getUsername() != null) {
            acc.setUsername(dto.getUsername());
        }

        if (dto.getEmail() != null) {
            acc.setEmail(dto.getEmail());
        }

        if (dto.getPassword() != null) {
            acc.setPassword(encoder.encode(dto.getPassword()));
        }

        if (dto.getEnabled() != null) {
            acc.setEnabled(dto.getEnabled());
        }

        if (dto.getRole() != null) {
            acc.setRole(dto.getRole());
        }

        repo.save(acc);
        return new AccountResponseDTO(acc);
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new CustomExceptions.ResourceNotFoundException("Account not found");
        }
        repo.deleteById(id);
    }
}
