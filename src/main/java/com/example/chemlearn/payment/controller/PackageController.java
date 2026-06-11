package com.example.chemlearn.payment.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.chemlearn.core.shared.constants.ApiPaths;
import com.example.chemlearn.payment.dto.LearningPackageResponse;
import com.example.chemlearn.payment.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.API_VERSION + "/packages")
@RequiredArgsConstructor
public class PackageController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<List<LearningPackageResponse>> getPackages() {
        return ResponseEntity.ok(paymentService.getActivePackages());
    }

    @GetMapping("/{packageCode}")
    public ResponseEntity<LearningPackageResponse> getPackage(@PathVariable String packageCode) {
        return ResponseEntity.ok(paymentService.getPackage(packageCode));
    }
}
