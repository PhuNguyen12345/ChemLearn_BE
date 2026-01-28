package com.example.chemlearn.service.serviceImpl;

import com.example.chemlearn.dtos.auth.AuthResponseDTO;
import com.example.chemlearn.dtos.auth.LoginRequestDTO;
import com.example.chemlearn.dtos.auth.RegisterRequestDTO;
import com.example.chemlearn.entity.Account;
import com.example.chemlearn.enums.AccountRole;
import com.example.chemlearn.repository.AccountRepository;
import com.example.chemlearn.service.AuthService;
import com.example.chemlearn.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AccountRepository repo;
    private final PasswordEncoder encoder;
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
        acc.setPassword(encoder.encode(dto.getPassword()));
        acc.setRole(AccountRole.ROLE_STUDENT);

        repo.save(acc);
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO dto) {

        System.out.println("STEP 1: Login attempt");

        Account acc = repo.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        System.out.println("STEP 2: Account found");
        System.out.println("  username = " + acc.getUsername());
        System.out.println("  email    = " + acc.getEmail());
        System.out.println("  role     = " + acc.getRole());
        System.out.println("  enabled  = " + acc.isEnabled());

        System.out.println("STEP 3: Checking password");
        boolean match = encoder.matches(dto.getPassword(), acc.getPassword());
        System.out.println("Password match = " + match);

        if (!match) {
            throw new RuntimeException("Invalid credentials");
        }

        System.out.println("STEP 4: Generating JWT");
        String token = jwtUtil.generateToken(acc);

        System.out.println("STEP 5: JWT generated");

        return new AuthResponseDTO(
                token,
                acc.getUsername(),
                acc.getEmail(),
                acc.getRole().name()
        );
    }

}
