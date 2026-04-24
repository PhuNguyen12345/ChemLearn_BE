package com.example.chemlearn.controller;

import com.example.chemlearn.dtos.parent.ParentAssessmentDTO;
import com.example.chemlearn.dtos.parent.ParentChildDTO;
import com.example.chemlearn.dtos.parent.ParentChildPerformanceDTO;
import com.example.chemlearn.service.ParentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parent")
@PreAuthorize("hasRole('PARENT')")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;

    @GetMapping("/children")
    public List<ParentChildDTO> getChildren(Authentication authentication) {
        return parentService.getChildren(authentication.getName());
    }

    @GetMapping("/children/{childId}/performance")
    public ParentChildPerformanceDTO getPerformance(@PathVariable Long childId, Authentication authentication) {
        return parentService.getChildPerformance(authentication.getName(), childId);
    }

    @GetMapping("/children/{childId}/assessments")
    public List<ParentAssessmentDTO> getAssessments(@PathVariable Long childId, Authentication authentication) {
        return parentService.getChildAssessments(authentication.getName(), childId);
    }
}
