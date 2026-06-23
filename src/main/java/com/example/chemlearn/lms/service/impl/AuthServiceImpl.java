package com.example.chemlearn.lms.service.impl;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.chemlearn.core.entity.AccessRequest;
import com.example.chemlearn.core.entity.OtpVerification;
import com.example.chemlearn.core.entity.Parent;
import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.core.entity.Teacher;
import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.core.enums.AuthProvider;
import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.lms.dto.core.auth.AuthResponseDTO;
import com.example.chemlearn.lms.dto.core.auth.GoogleLoginRequestDTO;
import com.example.chemlearn.lms.dto.core.auth.GoogleTokenInfo;
import com.example.chemlearn.lms.dto.core.auth.LoginRequestDTO;
import com.example.chemlearn.lms.dto.core.auth.OtpVerifyRequestDTO;
import com.example.chemlearn.lms.dto.core.auth.RegisterRequestDTO;
import com.example.chemlearn.lms.exception.CustomExceptions;
import com.example.chemlearn.lms.repository.AccessRequestRepository;
import com.example.chemlearn.lms.repository.OtpVerificationRepository;
import com.example.chemlearn.lms.repository.ParentRepository;
import com.example.chemlearn.lms.repository.StudentRepository;
import com.example.chemlearn.lms.repository.TeacherRepository;
import com.example.chemlearn.lms.repository.UserRepository;
import com.example.chemlearn.lms.service.AuthService;
import com.example.chemlearn.lms.service.EmailService;
import com.example.chemlearn.lms.service.OtpRateLimitService;
import com.example.chemlearn.util.JwtUtil;
import static com.example.chemlearn.util.PasswordUtil.hash;
import static com.example.chemlearn.util.PasswordUtil.matches;
import com.example.chemlearn.gamification.service.QuestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ParentRepository parentRepository;
    private final UserRepository repo;
    private final AccessRequestRepository accessRequestRepository;
    private final JwtUtil jwtUtil;
    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final OtpVerificationRepository otpVerificationRepository;
    private final EmailService emailService;
    private final OtpRateLimitService otpRateLimitService;
    private final ObjectMapper objectMapper;
    private final QuestService questService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${auth.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    @Value("${auth.lockout.max-attempts:5}")
    private int lockoutMaxAttempts;

    @Value("${auth.lockout.duration-minutes:15}")
    private int lockoutDurationMinutes;

    // ===== Existing student registration (unchanged) =====

    @Override
    public void register(RegisterRequestDTO dto) {
        if (repo.existsByUsername(dto.getUsername())) {
            throw new CustomExceptions.BadRequestException("Username exists");
        }
        if (repo.existsByEmail(dto.getEmail())) {
            throw new CustomExceptions.BadRequestException("Email exists");
        }
        if (dto.getGradeLevel() == null || dto.getGradeLevel() < 6 || dto.getGradeLevel() > 12) {
            throw new CustomExceptions.BadRequestException("Grade level must be between 6 and 12");
        }
        if (dto.getGender() == null || dto.getGender().isBlank()) {
            throw new CustomExceptions.BadRequestException("Gender is required");
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
        user.setGender(dto.getGender().trim());

        student.setUsers(user);
        student.setGradeLevel(dto.getGradeLevel());
        student.setLastActiveDate(LocalDate.now());

        studentRepository.save(student);
        repo.save(user);
    }

    @Override
    @Transactional
    public void registerTeacherParentPending(RegisterRequestDTO dto) {
        String role = dto.getRole();
        if (!"ROLE_TEACHER".equals(role) && !"ROLE_PARENT".equals(role)) {
            throw new CustomExceptions.BadRequestException("Pending registration is only for teacher or parent roles");
        }

        if (dto.getPhoneNumber() == null || dto.getPhoneNumber().isBlank()) {
            throw new CustomExceptions.BadRequestException("Phone number is required");
        }
        if ("ROLE_TEACHER".equals(role)) {
            if (dto.getDegree() == null || dto.getDegree().isBlank()) {
                throw new CustomExceptions.BadRequestException("Degree is required for teachers");
            }
            if (dto.getSpecialization() == null || dto.getSpecialization().isBlank()) {
                throw new CustomExceptions.BadRequestException("Specialization is required for teachers");
            }
        } else if (dto.getJobTitle() == null || dto.getJobTitle().isBlank()) {
            throw new CustomExceptions.BadRequestException("Job title is required for parents");
        }

        if (repo.existsByUsername(dto.getUsername())) {
            throw new CustomExceptions.BadRequestException("Username exists");
        }
        if (repo.existsByEmail(dto.getEmail())) {
            throw new CustomExceptions.BadRequestException("Email exists");
        }

        accessRequestRepository.findByEmail(dto.getEmail()).ifPresent(existing -> {
            if ("PENDING".equals(existing.getStatus())) {
                throw new CustomExceptions.BadRequestException("A pending access request already exists for this email");
            }
        });

        UserRole targetRole = UserRole.valueOf(role);
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setPassword(hash(dto.getPassword()));
        user.setAuthProvider(AuthProvider.LOCAL);
        String fullName = dto.getFullName() == null ? null : dto.getFullName().trim();
        user.setFullName((fullName == null || fullName.isBlank()) ? dto.getUsername() : fullName);
        user.setRole(targetRole);
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.setIsActive(false);
        user.setFailedLoginAttempts(0);
        user.setLockoutUntil(null);
        user.setAvatarUrl(null);

        User savedUser = repo.save(user);

        if (targetRole == UserRole.ROLE_TEACHER) {
            Teacher teacher = new Teacher();
            teacher.setUsers(savedUser);
            teacher.setDegree(dto.getDegree());
            teacher.setSpecialization(dto.getSpecialization());
            teacher.setWorkplace(dto.getWorkplace());
            teacherRepository.save(teacher);
        } else {
            Parent parent = new Parent();
            parent.setUsers(savedUser);
            parent.setPhoneNumber(dto.getPhoneNumber());
            parent.setJobTitle(dto.getJobTitle());
            parentRepository.save(parent);
        }

        AccessRequest request = accessRequestRepository.findByEmail(dto.getEmail()).orElseGet(AccessRequest::new);
        request.setEmail(dto.getEmail());
        request.setRole(role);
        request.setFullName(user.getFullName());
        request.setStatus("PENDING");
        request.setAdditionalInfo(buildPendingRegistrationInfo(dto));
        accessRequestRepository.save(request);

        log.info("Pending {} account created for admin approval: {}", role, dto.getEmail());
    }

    // ===== New OTP-based registration for teacher/parent =====

    @Override
    @Transactional
    public void registerWithOtp(RegisterRequestDTO dto) {
        String normalizedEmail = otpRateLimitService.normalizeEmail(dto.getEmail());
        dto.setEmail(normalizedEmail);

        String role = dto.getRole();
        if (role == null || role.isBlank()) {
            role = "ROLE_STUDENT";
            dto.setRole(role);
        }
        if (!"ROLE_STUDENT".equals(role) && !"ROLE_TEACHER".equals(role) && !"ROLE_PARENT".equals(role)) {
            throw new CustomExceptions.BadRequestException("Role is invalid");
        }

        if ("ROLE_STUDENT".equals(role)) {
            if (dto.getGradeLevel() == null || dto.getGradeLevel() < 6 || dto.getGradeLevel() > 12) {
                throw new CustomExceptions.BadRequestException("Grade level must be between 6 and 12");
            }
            if (dto.getGender() == null || dto.getGender().isBlank()) {
                throw new CustomExceptions.BadRequestException("Gender is required");
            }
        } else if ("ROLE_TEACHER".equals(role)) {
            if (dto.getPhoneNumber() == null || dto.getPhoneNumber().isBlank()) {
                throw new CustomExceptions.BadRequestException("Phone number is required");
            }
            if (dto.getDegree() == null || dto.getDegree().isBlank()) {
                throw new CustomExceptions.BadRequestException("Degree is required for teachers");
            }
            if (dto.getSpecialization() == null || dto.getSpecialization().isBlank()) {
                throw new CustomExceptions.BadRequestException("Specialization is required for teachers");
            }
        } else {
            if (dto.getPhoneNumber() == null || dto.getPhoneNumber().isBlank()) {
                throw new CustomExceptions.BadRequestException("Phone number is required");
            }
            if (dto.getJobTitle() == null || dto.getJobTitle().isBlank()) {
                throw new CustomExceptions.BadRequestException("Job title is required for parents");
            }
        }

        // Check uniqueness
        if (repo.existsByUsername(dto.getUsername())) {
            throw new CustomExceptions.BadRequestException("Username exists");
        }
        if (repo.existsByEmail(dto.getEmail())) {
            throw new CustomExceptions.BadRequestException("Email exists");
        }
        otpRateLimitService.assertCanSend(normalizedEmail);

        // Generate OTP
        String otpCode = generateOtp();

        // Serialize registration data
        String registrationJson;
        try {
            registrationJson = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new CustomExceptions.BadRequestException("Failed to process registration data");
        }

        otpRateLimitService.invalidateOpenOtps(normalizedEmail);

        // Save OTP record
        Instant now = Instant.now();
        OtpVerification otp = new OtpVerification();
        otp.setEmail(normalizedEmail);
        otp.setOtpCode(otpCode);
        otp.setPendingRegistrationData(registrationJson);
        otp.setCreatedAt(now);
        otp.setExpiresAt(now.plus(Duration.ofMinutes(otpExpiryMinutes)));
        otp.setVerified(false);
        otpVerificationRepository.save(otp);

        // Send OTP email
        String fullName = dto.getFullName() != null ? dto.getFullName() : dto.getUsername();
        emailService.sendOtpEmail(normalizedEmail, fullName, otpCode, otp.getExpiresAt());

        log.info("OTP sent for registration to email: {}", normalizedEmail);
    }

    @Override
    @Transactional
    public void verifyOtpAndCreateAccount(OtpVerifyRequestDTO dto) {
        String normalizedEmail = otpRateLimitService.normalizeEmail(dto.getEmail());
        OtpVerification otp = otpVerificationRepository
                .findByEmailAndOtpCodeAndVerifiedFalse(normalizedEmail, dto.getOtpCode())
                .orElseThrow(() -> new CustomExceptions.BadRequestException("Invalid OTP code"));

        if (otp.getPendingRegistrationData() == null || otp.getPendingRegistrationData().isBlank()) {
            throw new CustomExceptions.BadRequestException("No pending registration found for this OTP");
        }

        // Check expiration
        if (otp.getExpiresAt() == null || otp.getExpiresAt().isBefore(Instant.now())) {
            throw new CustomExceptions.BadRequestException("OTP has expired. Please request a new one.");
        }

        // Deserialize registration data
        RegisterRequestDTO regDto;
        try {
            regDto = objectMapper.readValue(otp.getPendingRegistrationData(), RegisterRequestDTO.class);
        } catch (JsonProcessingException e) {
            throw new CustomExceptions.BadRequestException("Failed to process registration data");
        }

        // Re-check uniqueness (in case someone registered between OTP send and verify)
        if (repo.existsByUsername(regDto.getUsername())) {
            throw new CustomExceptions.BadRequestException("Username is no longer available");
        }
        if (repo.existsByEmail(regDto.getEmail())) {
            throw new CustomExceptions.BadRequestException("Email is no longer available");
        }

        // Create user
        UserRole targetRole = UserRole.valueOf(regDto.getRole());
        User user = new User();
        user.setEmail(regDto.getEmail());
        user.setUsername(regDto.getUsername());
        user.setPassword(hash(regDto.getPassword()));
        user.setAuthProvider(AuthProvider.LOCAL);
        String fullName = regDto.getFullName() == null ? null : regDto.getFullName().trim();
        user.setFullName((fullName == null || fullName.isBlank()) ? regDto.getUsername() : fullName);
        user.setRole(targetRole);
        user.setPhoneNumber(regDto.getPhoneNumber());
        if (targetRole == UserRole.ROLE_STUDENT && regDto.getGender() != null) {
            user.setGender(regDto.getGender().trim());
        }
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.setIsActive(true);
        user.setFailedLoginAttempts(0);
        user.setLockoutUntil(null);
        user.setAvatarUrl(null);

        User savedUser = repo.save(user);

        // Create role-specific entity
        if (targetRole == UserRole.ROLE_STUDENT) {
            Student student = new Student();
            student.setUsers(savedUser);
            student.setGradeLevel(regDto.getGradeLevel());
            student.setLastActiveDate(LocalDate.now());
            studentRepository.save(student);
        } else if (targetRole == UserRole.ROLE_TEACHER) {
            Teacher teacher = new Teacher();
            teacher.setUsers(savedUser);
            teacher.setDegree(regDto.getDegree());
            teacher.setSpecialization(regDto.getSpecialization());
            teacherRepository.save(teacher);
        } else if (targetRole == UserRole.ROLE_PARENT) {
            Parent parent = new Parent();
            parent.setUsers(savedUser);
            parent.setPhoneNumber(regDto.getPhoneNumber());
            parent.setJobTitle(regDto.getJobTitle());
            parentRepository.save(parent);
        }

        // Mark OTP as verified and clean up
        otp.setVerified(true);
        otpVerificationRepository.save(otp);

        log.info("Account created via OTP verification for: {} (role: {})", regDto.getEmail(), regDto.getRole());
    }

    @Override
    @Transactional
    public void resendOtp(String email) {
        String normalizedEmail = otpRateLimitService.normalizeEmail(email);

        OtpVerification existing = otpVerificationRepository
                .findTopByEmailOrderByCreatedAtDesc(normalizedEmail)
                .orElseThrow(() -> new CustomExceptions.BadRequestException("No pending registration found for this email"));

        if (Boolean.TRUE.equals(existing.getVerified())) {
            throw new CustomExceptions.BadRequestException("This email has already been verified");
        }
        if (existing.getPendingRegistrationData() == null || existing.getPendingRegistrationData().isBlank()) {
            throw new CustomExceptions.BadRequestException("No pending registration found for this email");
        }

        otpRateLimitService.assertCanSend(normalizedEmail);

        // Generate new OTP
        String newOtp = generateOtp();
        String pendingRegistrationData = existing.getPendingRegistrationData();
        otpRateLimitService.invalidateOpenOtps(normalizedEmail);

        Instant now = Instant.now();
        OtpVerification resend = new OtpVerification();
        resend.setEmail(normalizedEmail);
        resend.setOtpCode(newOtp);
        resend.setPendingRegistrationData(pendingRegistrationData);
        resend.setCreatedAt(now);
        resend.setExpiresAt(now.plus(Duration.ofMinutes(otpExpiryMinutes)));
        resend.setVerified(false);
        otpVerificationRepository.save(resend);

        // Deserialize to get the full name for the email
        String fullName = normalizedEmail;
        try {
            RegisterRequestDTO regDto = objectMapper.readValue(pendingRegistrationData, RegisterRequestDTO.class);
            if (regDto.getFullName() != null && !regDto.getFullName().isBlank()) {
                fullName = regDto.getFullName();
            }
        } catch (JsonProcessingException e) {
            // Use email as fallback
        }

        emailService.sendOtpEmail(normalizedEmail, fullName, newOtp, resend.getExpiresAt());
        log.info("OTP resent to: {}", normalizedEmail);
    }

    // ===== Login (unchanged) =====

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
    public AuthResponseDTO loginWithGoogle(GoogleLoginRequestDTO dto) {
        GoogleTokenInfo tokenInfo = googleTokenVerifierService.verify(dto.getIdToken());

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
                user = createUserFromGoogle(tokenInfo, dto.getGradeLevel(), dto.getGender());
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

    // ===== Private helpers =====

    private String generateOtp() {
        int otp = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(otp);
    }

    private String buildPendingRegistrationInfo(RegisterRequestDTO dto) {
        StringBuilder info = new StringBuilder();
        appendInfo(info, "Username", dto.getUsername());
        appendInfo(info, "Phone number", dto.getPhoneNumber());
        if ("ROLE_TEACHER".equals(dto.getRole())) {
            appendInfo(info, "Workplace", dto.getWorkplace());
            appendInfo(info, "Degree", dto.getDegree());
            appendInfo(info, "Specialization", dto.getSpecialization());
        } else if ("ROLE_PARENT".equals(dto.getRole())) {
            appendInfo(info, "Job title", dto.getJobTitle());
        }
        return info.toString().trim();
    }

    private void appendInfo(StringBuilder info, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (info.length() > 0) {
            info.append('\n');
        }
        info.append(label).append(": ").append(value.trim());
    }

    private AuthResponseDTO buildAuthResponse(User user) {
        AuthResponseDTO dto = new AuthResponseDTO();
        dto.setToken(jwtUtil.generateToken(user));
        dto.setId(user.getId() != null ? user.getId().toString() : null);
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setFullName(user.getFullName());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setIsActive(user.getIsActive());

        // Track LOGIN daily quest progress if user is a student
        if (user.getRole() == UserRole.ROLE_STUDENT) {
            try {
                questService.updateProgress(user.getId(), "LOGIN", 1);
            } catch (Exception e) {
                log.error("Failed to track LOGIN quest progress", e);
            }
        }

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
        Integer attempts = user.getFailedLoginAttempts();
        if (attempts == null) {
            attempts = 0;
        }
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

    private User createUserFromGoogle(GoogleTokenInfo tokenInfo, Integer gradeLevel, String gender) {
        if (gradeLevel == null || gradeLevel < 6 || gradeLevel > 12) {
            throw new CustomExceptions.BadRequestException("Google signup requires grade level and gender");
        }
        if (gender == null || gender.isBlank()) {
            throw new CustomExceptions.BadRequestException("Google signup requires grade level and gender");
        }

        User user = new User();
        user.setEmail(tokenInfo.getEmail());
        user.setUsername(generateUniqueUsername(tokenInfo));
        user.setFullName(resolveFullName(tokenInfo));
        user.setPassword(hash(UUID.randomUUID().toString()));
        user.setRole(UserRole.ROLE_STUDENT);
        user.setAuthProvider(AuthProvider.GOOGLE);
        user.setProviderSubject(tokenInfo.getSub());
        user.setAvatarUrl(tokenInfo.getPicture());
        user.setGender(gender.trim());
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.setIsActive(true);
        user.setFailedLoginAttempts(0);
        user.setLockoutUntil(null);
        user.setLastFailedAt(null);

        Student student = new Student();
        student.setUsers(user);
        student.setGradeLevel(gradeLevel);
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
