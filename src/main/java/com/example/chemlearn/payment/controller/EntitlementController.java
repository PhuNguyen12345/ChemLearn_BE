package com.example.chemlearn.payment.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.chemlearn.core.shared.constants.ApiPaths;
import com.example.chemlearn.payment.dto.UserPackageEntitlementResponse;
import com.example.chemlearn.payment.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.API_VERSION + "/me/entitlements")
@RequiredArgsConstructor
public class EntitlementController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<List<UserPackageEntitlementResponse>> getMyEntitlements() {
        return ResponseEntity.ok(paymentService.getCurrentUserEntitlements());
    }

    @GetMapping("/{packageCode}")
    public ResponseEntity<UserPackageEntitlementResponse> getMyEntitlement(@PathVariable String packageCode) {
        return ResponseEntity.ok(paymentService.getCurrentUserEntitlement(packageCode));
    }

    @PatchMapping("/{entitlementId}/cancel")
    public ResponseEntity<UserPackageEntitlementResponse> cancelMyEntitlement(@PathVariable UUID entitlementId) {
        return ResponseEntity.ok(paymentService.cancelCurrentUserEntitlement(entitlementId));
    }
}
