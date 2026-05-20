package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.core.enums.AuthProvider;
import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.lms.dto.core.auth.AuthResponseDTO;
import com.example.chemlearn.lms.dto.core.auth.LoginRequestDTO;
import com.example.chemlearn.lms.dto.core.auth.RegisterRequestDTO;
import com.example.chemlearn.lms.dto.core.auth.GoogleTokenInfo;
import com.example.chemlearn.lms.exception.CustomExceptions;
import com.example.chemlearn.lms.repository.StudentRepository;
import com.example.chemlearn.lms.repository.UserRepository;
import com.example.chemlearn.lms.service.AuthService;
import com.example.chemlearn.util.JwtUtil;
import com.example.chemlearn.lms.service.impl.GoogleTokenVerifierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;

import static com.example.chemlearn.util.PasswordUtil.hash;
import static com.example.chemlearn.util.PasswordUtil.matches;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final StudentRepository studentRepository;
    private final UserRepository repo;
    private final JwtUtil jwtUtil;
    private final GoogleTokenVerifierService googleTokenVerifierService;

    @Value("${auth.lockout.max-attempts:5}")
    private int lockoutMaxAttempts;

    @Value("${auth.lockout.duration-minutes:15}")
    private int lockoutDurationMinutes;

    @Override
    public void register(RegisterRequestDTO dto) {
        if (repo.existsByUsername(dto.getUsername())) {
            throw new CustomExceptions.BadRequestException("Username exists");
        }
        if (repo.existsByEmail(dto.getEmail())) {
            throw new CustomExceptions.BadRequestException("Email exists");
        }
        User user = new User();
        Student student = new Student();
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setPassword(hash(dto.getPassword()));
        user.setAuthProvider(AuthProvider.LOCAL);
        String fullName = dto.getFullName() == null ? null : dto.getFullName().trim();
        user.setFullName((fullName == null || fullName.isBlank()) ? dto.getUsername() : fullName);
        user.setRole(UserRole.ROLE_STUDENT);

        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.setIsActive(true);
        user.setFailedLoginAttempts(0);
        user.setLockoutUntil(null);
        //TODO: auto assign avatar
        user.setAvatarUrl(null);

        student.setUsers(user);
        student.setGradeLevel(0);
        student.setLastActiveDate(LocalDate.now());

        studentRepository.save(student);
        repo.save(user);
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO dto) {
        User acc = repo.findByUsername(dto.getUsername())
                .orElseThrow(() -> new CustomExceptions.UnauthorizedException("Invalid credentials"));

        ensureNotLocked(acc);

        if (Boolean.FALSE.equals(acc.getIsActive())) {
            throw new CustomExceptions.UnauthorizedException("Account is disabled");
        }

        if (!matches(dto.getPassword(), acc.getPassword())) {
            registerFailedAttempt(acc);
            throw new CustomExceptions.UnauthorizedException("Invalid credentials");
        }

        resetLoginAttempts(acc);
        return buildAuthResponse(acc);
    }

    @Override
    public AuthResponseDTO loginWithGoogle(String idToken) {
        GoogleTokenInfo tokenInfo = googleTokenVerifierService.verify(idToken);

        if (tokenInfo.getEmail() == null || tokenInfo.getEmail().isBlank()) {
            throw new CustomExceptions.UnauthorizedException("Google token missing email");
        }
        if (tokenInfo.getSub() == null || tokenInfo.getSub().isBlank()) {
            throw new CustomExceptions.UnauthorizedException("Google token missing subject");
        }

        User user = repo.findByProviderSubject(tokenInfo.getSub()).orElse(null);

        if (user == null) {
            user = repo.findByEmail(tokenInfo.getEmail()).orElse(null);
            if (user != null) {
                user.setAuthProvider(AuthProvider.GOOGLE);
                user.setProviderSubject(tokenInfo.getSub());
                user = repo.save(user);
            } else {
                user = createUserFromGoogle(tokenInfo);
            }
        }

        ensureNotLocked(user);
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new CustomExceptions.UnauthorizedException("Account is disabled");
        }

        resetLoginAttempts(user);
        return buildAuthResponse(user);
    }

    @Override
    public void logout(String token) {
        // Stateless JWT logout is handled client-side.
    }

    private AuthResponseDTO buildAuthResponse(User user) {
        AuthResponseDTO dto = new AuthResponseDTO();
        dto.setToken(jwtUtil.generateToken(user));
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setFullName(user.getFullName());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setIsActive(user.getIsActive());
        return dto;
    }

    private void ensureNotLocked(User user) {
        Instant lockoutUntil = user.getLockoutUntil();
        if (lockoutUntil != null && lockoutUntil.isAfter(Instant.now())) {
            throw new CustomExceptions.AccountLockedException("Account is temporarily locked. Try again later.");
        }
        if (lockoutUntil != null && lockoutUntil.isBefore(Instant.now())) {
            user.setLockoutUntil(null);
            user.setFailedLoginAttempts(0);
            user.setLastFailedAt(null);
            repo.save(user);
        }
    }

    private void registerFailedAttempt(User user) {
        int attempts = user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts();
        attempts += 1;
        user.setFailedLoginAttempts(attempts);
        user.setLastFailedAt(Instant.now());
        if (attempts >= lockoutMaxAttempts) {
            user.setLockoutUntil(Instant.now().plus(Duration.ofMinutes(lockoutDurationMinutes)));
        }
        repo.save(user);
    }

    private void resetLoginAttempts(User user) {
        if (user.getFailedLoginAttempts() != null && user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            user.setLockoutUntil(null);
            user.setLastFailedAt(null);
            repo.save(user);
        }
    }

    private User createUserFromGoogle(GoogleTokenInfo tokenInfo) {
        User user = new User();
        user.setEmail(tokenInfo.getEmail());
        user.setUsername(generateUniqueUsername(tokenInfo));
        user.setFullName(resolveFullName(tokenInfo));
        user.setPassword(hash(UUID.randomUUID().toString()));
        user.setRole(UserRole.ROLE_STUDENT);
        user.setAuthProvider(AuthProvider.GOOGLE);
        user.setProviderSubject(tokenInfo.getSub());
        user.setAvatarUrl(tokenInfo.getPicture());
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.setIsActive(true);
        user.setFailedLoginAttempts(0);
        user.setLockoutUntil(null);
        user.setLastFailedAt(null);

        Student student = new Student();
        student.setUsers(user);
        student.setGradeLevel(0);
        student.setLastActiveDate(LocalDate.now());
        studentRepository.save(student);

        return repo.save(user);
    }

    private String resolveFullName(GoogleTokenInfo tokenInfo) {
        if (tokenInfo.getName() != null && !tokenInfo.getName().isBlank()) {
            return tokenInfo.getName().trim();
        }
        String email = tokenInfo.getEmail();
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@")).trim();
        }
        return "Google User";
    }

    private String generateUniqueUsername(GoogleTokenInfo tokenInfo) {
        String base = "user";
        if (tokenInfo.getEmail() != null && tokenInfo.getEmail().contains("@")) {
            base = tokenInfo.getEmail().substring(0, tokenInfo.getEmail().indexOf("@"));
        } else if (tokenInfo.getName() != null && !tokenInfo.getName().isBlank()) {
            base = tokenInfo.getName();
        }

        base = base.toLowerCase(Locale.US).replaceAll("[^a-z0-9]", "");
        if (base.isBlank()) {
            base = "user";
        }

        String candidate = base;
        int suffix = 1;
        while (repo.existsByUsername(candidate)) {
            candidate = base + suffix;
            suffix += 1;
        }

        return candidate;
    }
}
