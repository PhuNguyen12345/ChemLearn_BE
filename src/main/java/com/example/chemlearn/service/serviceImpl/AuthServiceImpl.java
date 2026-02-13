package com.example.chemlearn.service.serviceImpl;

import com.example.chemlearn.dtos.auth.AuthResponseDTO;
import com.example.chemlearn.dtos.auth.LoginRequestDTO;
import com.example.chemlearn.dtos.auth.RegisterRequestDTO;
import com.example.chemlearn.entity.Account;
import com.example.chemlearn.entity.Teacher;
import com.example.chemlearn.entity.User;
import com.example.chemlearn.enums.AccountRole;
import com.example.chemlearn.enums.Role;
import com.example.chemlearn.repository.AccountRepository;
import com.example.chemlearn.repository.TeacherRepository;
import com.example.chemlearn.repository.UserRepository;
import com.example.chemlearn.service.AuthService;
import com.example.chemlearn.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final TeacherRepository teacherRepository;

    @Override
    public void register(RegisterRequestDTO dto) {

        if (repo.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username exists");
        }
        if (repo.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email exists");
        }

        Teacher teacher = new Teacher();
        teacher.setEmail(dto.getEmail());
        teacher.setUsername(dto.getUsername());
        teacher.setPassword(encoder.encode(dto.getPassword()));
        teacher.setRole(Role.TEACHER);
        teacherRepository.save(teacher);
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO dto) {

        System.out.println("STEP 1: Login attempt");

        User user = repo.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        System.out.println("STEP 2: User found");
        System.out.println("  username = " + user.getUsername());
        System.out.println("  email    = " + user.getEmail());
        System.out.println("  role     = " + user.getRole());
        System.out.println("  enabled  = " + user.isEnabled());

        System.out.println("STEP 3: Checking password");
        boolean match = encoder.matches(dto.getPassword(), user.getPassword());
        System.out.println("Password match = " + match);

        if (!match) {
            throw new RuntimeException("Invalid credentials");
        }

        System.out.println("STEP 4: Generating JWT");
        String token = jwtUtil.generateToken(user);

        System.out.println("STEP 5: JWT generated");

        return new AuthResponseDTO(
                token,
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );
    }

}
