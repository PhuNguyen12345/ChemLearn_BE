package com.example.chemlearn.lms.service.impl;

import com.example.chemlearn.core.entity.AccessRequest;
import com.example.chemlearn.core.entity.Invite;
import com.example.chemlearn.core.entity.Parent;
import com.example.chemlearn.core.entity.Teacher;
import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.core.enums.AuthProvider;
import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.lms.dto.core.auth.AccessRequestCreateDTO;
import com.example.chemlearn.lms.dto.core.auth.InviteAcceptRequestDTO;
import com.example.chemlearn.lms.exception.CustomExceptions;
import com.example.chemlearn.lms.repository.AccessRequestRepository;
import com.example.chemlearn.lms.repository.InviteRepository;
import com.example.chemlearn.lms.repository.ParentRepository;
import com.example.chemlearn.lms.repository.TeacherRepository;
import com.example.chemlearn.lms.repository.UserRepository;
import com.example.chemlearn.lms.service.AuthOnboardingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.example.chemlearn.util.PasswordUtil.hash;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthOnboardingServiceImpl implements AuthOnboardingService {

    private final AccessRequestRepository accessRequestRepository;
    private final InviteRepository inviteRepository;
    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final ParentRepository parentRepository;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Override
    @Transactional
    public void submitAccessRequest(AccessRequestCreateDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new CustomExceptions.BadRequestException("Email is already registered");
        }

        // Check if there is already a PENDING access request for this email
        accessRequestRepository.findByEmail(dto.getEmail()).ifPresent(req -> {
            if ("PENDING".equals(req.getStatus())) {
                throw new CustomExceptions.BadRequestException("A pending access request already exists for this email");
            }
        });

        AccessRequest request = new AccessRequest();
        request.setEmail(dto.getEmail());
        request.setRole(dto.getRole());
        request.setFullName(dto.getFullName());
        request.setAdditionalInfo(dto.getAdditionalInfo());
        request.setStatus("PENDING");

        accessRequestRepository.save(request);
    }

    @Override
    @Transactional
    public void acceptInvite(InviteAcceptRequestDTO dto) {
        Invite invite = inviteRepository.findByToken(dto.getToken())
                .orElseThrow(() -> new CustomExceptions.BadRequestException("Invalid invite token"));

        if (!"PENDING".equals(invite.getStatus())) {
            throw new CustomExceptions.BadRequestException("Invite token is already used or invalid");
        }

        if (invite.getExpiresAt().isBefore(Instant.now())) {
            invite.setStatus("EXPIRED");
            inviteRepository.save(invite);
            throw new CustomExceptions.BadRequestException("Invite token has expired");
        }

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new CustomExceptions.BadRequestException("Username already exists");
        }
        if (userRepository.existsByEmail(invite.getEmail())) {
            throw new CustomExceptions.BadRequestException("Email already registered");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(invite.getEmail());
        user.setPassword(hash(dto.getPassword()));
        user.setFullName(dto.getFullName() != null && !dto.getFullName().isBlank() ? dto.getFullName() : invite.getEmail());
        
        UserRole targetRole = UserRole.valueOf(invite.getRole());
        user.setRole(targetRole);
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        if (targetRole == UserRole.ROLE_TEACHER) {
            Teacher teacher = new Teacher();
            teacher.setUsers(savedUser);
            teacher.setBio(dto.getBio());
            teacher.setSpecialization(dto.getSpecialization());
            teacher.setDegree(dto.getDegree());
            teacher.setWorkplace(dto.getWorkplace());
            teacherRepository.save(teacher);
        } else if (targetRole == UserRole.ROLE_PARENT) {
            Parent parent = new Parent();
            parent.setUsers(savedUser);
            parent.setPhoneNumber(dto.getPhoneNumber());
            parent.setJobTitle(dto.getJobTitle());
            parentRepository.save(parent);
        }

        invite.setStatus("ACCEPTED");
        inviteRepository.save(invite);

        // Also update any matching access request status to APPROVED
        accessRequestRepository.findByEmail(invite.getEmail()).ifPresent(req -> {
            req.setStatus("APPROVED");
            accessRequestRepository.save(req);
        });
    }

    @Override
    public List<AccessRequest> getAllRequests() {
        return accessRequestRepository.findAll();
    }

    @Override
    @Transactional
    public void approveRequest(UUID requestId) {
        AccessRequest request = accessRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Access request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new CustomExceptions.BadRequestException("Request is not pending");
        }

        // Create the invite
        Invite invite = createInvite(request.getEmail(), request.getRole());
        request.setStatus("APPROVED");
        accessRequestRepository.save(request);
    }

    @Override
    @Transactional
    public void rejectRequest(UUID requestId) {
        AccessRequest request = accessRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Access request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new CustomExceptions.BadRequestException("Request is not pending");
        }

        request.setStatus("REJECTED");
        accessRequestRepository.save(request);
    }

    @Override
    public List<Invite> getAllInvites() {
        return inviteRepository.findAll();
    }

    @Override
    @Transactional
    public Invite createInvite(String email, String role) {
        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new CustomExceptions.BadRequestException("User with this email already exists");
        }

        // If a pending invite exists, delete or expire it
        inviteRepository.findByEmail(email).ifPresent(invite -> {
            if ("PENDING".equals(invite.getStatus())) {
                invite.setStatus("EXPIRED");
                inviteRepository.save(invite);
            }
        });

        String token = UUID.randomUUID().toString();
        Invite invite = new Invite();
        invite.setEmail(email);
        invite.setRole(role);
        invite.setToken(token);
        invite.setStatus("PENDING");
        invite.setExpiresAt(Instant.now().plus(Duration.ofDays(1)));

        Invite savedInvite = inviteRepository.save(invite);

        // Send email (with fallback logging)
        sendInviteEmail(savedInvite);

        return savedInvite;
    }

    private void sendInviteEmail(Invite invite) {
        String inviteUrl = frontendBaseUrl + "/invite?token=" + invite.getToken();
        log.info("Email service is disabled. Invite link generated for {}: {}", invite.getEmail(), inviteUrl);
        System.out.println("=================================================");
        System.out.println("INVITE LINK GENERATED FOR EMAIL: " + invite.getEmail());
        System.out.println("LINK: " + inviteUrl);
        System.out.println("=================================================");
    }
}
