package com.example.chemlearn.lms.service;

import com.example.chemlearn.core.entity.AccessRequest;
import com.example.chemlearn.core.entity.Invite;
import com.example.chemlearn.lms.dto.core.auth.AccessRequestCreateDTO;
import com.example.chemlearn.lms.dto.core.auth.InviteAcceptRequestDTO;

import java.util.List;
import java.util.UUID;

public interface AuthOnboardingService {
    void submitAccessRequest(AccessRequestCreateDTO dto);
    void acceptInvite(InviteAcceptRequestDTO dto);
    
    List<AccessRequest> getAllRequests();
    void approveRequest(UUID requestId);
    void rejectRequest(UUID requestId);
    
    List<Invite> getAllInvites();
    Invite createInvite(String email, String role);
}
