package com.example.chemlearn.lab.controller;

import com.example.chemlearn.core.response.PageResponse;
import com.example.chemlearn.lab.dto.request.SaveProgressRequest;
import com.example.chemlearn.lab.dto.response.LabPlayResponse;
import com.example.chemlearn.lab.dto.response.LabSummaryResponse;
import com.example.chemlearn.lab.enums.LabCategory;
import com.example.chemlearn.lab.enums.LabType;
import com.example.chemlearn.lab.service.LabProgressService;
import com.example.chemlearn.lab.service.LabService;
import com.example.chemlearn.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Controller
@AllArgsConstructor
@RequestMapping("/api/v1/student/virtual-labs")
public class LabController {
    private LabService labService;
    private LabProgressService  labProgressService;
    @GetMapping
    public ResponseEntity<PageResponse<LabSummaryResponse>> getLabs(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false) LabType type,
            @RequestParam(required = false) LabCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // MOCK USER: Đảm bảo ID này ĐÃ TỒN TẠI trong bảng Users của sếp
//        UUID currentStudentId = UUID.fromString("89dc2dd7-9629-4e8a-8b70-6a6256460efd");
        UUID currentStudentId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(labService.findLabs(currentStudentId, keyword, category, type, page, size));
    }

    @GetMapping("/{labId}")
    public ResponseEntity<LabPlayResponse> enterLab(@PathVariable("labId") UUID labId) {
//        UUID currentStudentId = UUID.fromString("89dc2dd7-9629-4e8a-8b70-6a6256460efd");
        UUID currentStudentId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(labProgressService.playLab(labId, currentStudentId));
    }

    @PutMapping("/{labId}/progress")
    public ResponseEntity<String> saveProgress(
            @PathVariable("labId") UUID labId,
            @RequestBody SaveProgressRequest request) {
        //Use student id
//        UUID currentStudentId = UUID.fromString("89dc2dd7-9629-4e8a-8b70-6a6256460efd");
        UUID currentStudentId = SecurityUtils.getCurrentUserId();
        //save
        labProgressService.saveProgress(currentStudentId, labId, request);
        return ResponseEntity.ok("Đã lưu tiến trình thành công!");
    }
    @PostMapping("/{labId}/submit")
    public ResponseEntity<String> submitLab(@PathVariable UUID labId) {
        //Use student id
//        UUID currentStudentId = UUID.fromString("89dc2dd7-9629-4e8a-8b70-6a6256460efd");
        UUID currentStudentId = SecurityUtils.getCurrentUserId();
        //submit
        labProgressService.submitLab(currentStudentId, labId);
        return ResponseEntity.ok("Nộp bài thí nghiệm thành công.");
    }

    @DeleteMapping("/{labId}/reset")
    public ResponseEntity<String> resetLab(@PathVariable UUID labId) {
        //Use student id
//        UUID currentStudentId = UUID.fromString("89dc2dd7-9629-4e8a-8b70-6a6256460efd");
        UUID currentStudentId = SecurityUtils.getCurrentUserId();
        //reset lab
        labProgressService.resetLab(currentStudentId, labId);
        //return message
        return ResponseEntity.ok("Đã reset lại bài thí nghiệm.");
    }

    @PatchMapping("/{labId}/rename")
    public ResponseEntity<String> renameLab(@PathVariable UUID labId,
                                            @RequestParam String newTitle) {
        UUID currentStudentId = SecurityUtils.getCurrentUserId();
        labService.renameSandboxLab(currentStudentId, labId, newTitle);
        return ResponseEntity.ok("Đã sửa tên bài lab thành công.");
    }

    @PostMapping("/sandbox")
    public ResponseEntity<Map<String, UUID>> createSandboxLab() {
        UUID currentStudentId = SecurityUtils.getCurrentUserId();
        UUID newLabId = labService.createSandboxLab(currentStudentId);
        // Trả về JSON dạng {"labId": "xxx-yyy-zzz"} để FE lấy điều hướng
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Collections.singletonMap("labId", newLabId));
    }

}
