package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.core.entity.Account;
import com.example.chemlearn.lms.dto.core.auth.AuthResponseDTO;
import com.example.chemlearn.lms.dto.core.auth.LoginRequestDTO;
import com.example.chemlearn.lms.dto.core.auth.RegisterRequestDTO;
import com.example.chemlearn.lms.enums.AccountRole;
import com.example.chemlearn.lms.repository.AccountRepository;
import com.example.chemlearn.lms.service.AuthService;
import com.example.chemlearn.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.chemlearn.util.PasswordUtil.hash;
import static com.example.chemlearn.util.PasswordUtil.matches;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AccountRepository repo;
    private final JwtUtil jwtUtil;

    @Override
    public void register(RegisterRequestDTO dto) {
        if (repo.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username exists");
        }
        if (repo.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email exists");
        }
        Account acc = new Account();
        acc.setEmail(dto.getEmail());
        acc.setUsername(dto.getUsername());
        acc.setPassword(hash(dto.getPassword()));
        acc.setRole(AccountRole.ROLE_STUDENT);
        repo.save(acc);
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO dto) {
        Account acc = repo.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!matches(dto.getPassword(), acc.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        return new AuthResponseDTO(jwtUtil.generateToken(acc), acc.getUsername(), acc.getEmail(), acc.getRole().name());
    }

    @Override
    public void logout(String token) {
        // Stateless JWT logout is handled client-side.
    }
}
