package com.example.chemlearn.lms.service;

import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.lms.dto.core.AccountLinkConfirmDTO;
import com.example.chemlearn.lms.dto.core.AccountLinkInitiateDTO;
import com.example.chemlearn.lms.entity.AccountLinkRequest;
import com.example.chemlearn.lms.entity.ParentStudentLink;
import com.example.chemlearn.lms.repository.AccountLinkRequestRepository;
import com.example.chemlearn.lms.repository.ParentStudentLinkRepository;
import com.example.chemlearn.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountLinkService {
    private final AccountLinkRequestRepository linkRequestRepository;
    private final UserRepository userRepository;
    private final ParentStudentLinkRepository parentStudentLinkRepository;
    private final EmailService emailService;
    private final ParentStudentLinkService parentStudentLinkService;

    @Transactional
    public void initiateLink(String currentUsername, AccountLinkInitiateDTO dto) {
        User initiator = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Ensure target user exists and has correct role
        User targetUser = userRepository.findByEmailIgnoreCase(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với email này trong hệ thống."));
        
        if (initiator.getRole() == UserRole.ROLE_STUDENT && targetUser.getRole() != UserRole.ROLE_PARENT) {
            throw new RuntimeException("Tài khoản đích không phải là Phụ huynh.");
        }
        if (initiator.getRole() == UserRole.ROLE_PARENT && targetUser.getRole() != UserRole.ROLE_STUDENT) {
            throw new RuntimeException("Tài khoản đích không phải là Học sinh.");
        }

        // Prevent duplicate pending links
        Optional<AccountLinkRequest> existing = linkRequestRepository.findByInitiatorIdAndTargetEmailAndStatus(
                initiator.getId(), targetUser.getEmail(), "PENDING");
        if (existing.isPresent()) {
            throw new RuntimeException("Bạn đã gửi một yêu cầu liên kết đến email này và đang chờ xác nhận.");
        }

        // Check if already linked
        UUID studentUserId = initiator.getRole() == UserRole.ROLE_STUDENT ? initiator.getId() : targetUser.getId();
        UUID parentUserId = initiator.getRole() == UserRole.ROLE_PARENT ? initiator.getId() : targetUser.getId();

        Optional<ParentStudentLink> existingLink = parentStudentLinkRepository.findByParent_IdAndStudent_Id(parentUserId, studentUserId);
        if (existingLink.isPresent()) {
            throw new RuntimeException("Hai tài khoản này đã được liên kết với nhau từ trước.");
        }

        String token = UUID.randomUUID().toString();

        AccountLinkRequest request = AccountLinkRequest.builder()
                .initiatorId(initiator.getId())
                .targetEmail(targetUser.getEmail())
                .token(token)
                .status("PENDING")
                .createdAt(Instant.now())
                .build();

        linkRequestRepository.save(request);

        // Send email asynchronously - does NOT block response
        emailService.sendLinkConfirmationEmail(targetUser.getEmail(), initiator.getFullName(), token);
    }

    @Transactional
    public void confirmLink(AccountLinkConfirmDTO dto) {
        AccountLinkRequest request = linkRequestRepository.findByToken(dto.getToken())
                .orElseThrow(() -> new RuntimeException("Link xác nhận không hợp lệ hoặc đã hết hạn."));

        // Idempotent: if already accepted, just return success
        if (request.getStatus().equals("ACCEPTED")) {
            return;
        }

        if (!request.getStatus().equals("PENDING")) {
            throw new RuntimeException("Yêu cầu liên kết này đã bị từ chối.");
        }

        User targetUser = userRepository.findByEmailIgnoreCase(request.getTargetEmail())
                .orElseThrow(() -> new RuntimeException("Tài khoản người nhận không tồn tại."));

        User initiator = userRepository.findById(request.getInitiatorId())
                .orElseThrow(() -> new RuntimeException("Người gửi yêu cầu không tồn tại."));

        UUID studentUserId = initiator.getRole() == UserRole.ROLE_STUDENT ? initiator.getId() : targetUser.getId();
        UUID parentUserId = initiator.getRole() == UserRole.ROLE_PARENT ? initiator.getId() : targetUser.getId();

        User studentUser = userRepository.findById(studentUserId).orElseThrow();
        User parentUser = userRepository.findById(parentUserId).orElseThrow();

        // Use REQUIRES_NEW service so a duplicate-key exception there
        // does NOT mark THIS transaction as rollback-only.
        try {
            parentStudentLinkService.ensureLinkExists(parentUser, studentUser, parentUserId, studentUserId);
        } catch (Exception e) {
            log.warn("ensureLinkExists threw (link likely already exists): {}", e.getMessage());
        }

        // Always mark request as ACCEPTED regardless of whether link was new or already existed
        request.setStatus("ACCEPTED");
        linkRequestRepository.save(request);
    }

    public List<AccountLinkRequest> getPendingLinks(String currentUsername) {
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return linkRequestRepository.findByInitiatorIdAndStatus(currentUser.getId(), "PENDING");
    }
}
