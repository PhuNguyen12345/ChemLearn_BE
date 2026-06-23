package com.example.chemlearn.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.chemlearn.core.shared.constants.ApiPaths;
import com.example.chemlearn.payment.dto.CheckoutResponseData;
import com.example.chemlearn.payment.dto.PaymentData;
import com.example.chemlearn.payment.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.PAYMENT_PREFIX)
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-link")
    public ResponseEntity<CheckoutResponseData> createPaymentLink(@Valid @RequestBody PaymentData request) {
        return ResponseEntity.ok(paymentService.createPaymentLink(request));
    }

    @GetMapping("/{orderCode}")
    public ResponseEntity<CheckoutResponseData> getPaymentStatus(@PathVariable Long orderCode) {
        return ResponseEntity.ok(paymentService.getPaymentStatus(orderCode));
    }
}
