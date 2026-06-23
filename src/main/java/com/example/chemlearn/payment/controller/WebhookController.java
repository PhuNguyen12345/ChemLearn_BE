package com.example.chemlearn.payment.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.chemlearn.core.shared.constants.ApiPaths;
import com.example.chemlearn.payment.dto.CheckoutResponseData;
import com.example.chemlearn.payment.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.PAYMENT_PREFIX + "/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final PaymentService paymentService;

    @PostMapping("/payos")
    public ResponseEntity<Map<String, Object>> handlePayOSWebhook(@RequestBody Map<String, Object> webhookBody) {
        CheckoutResponseData response = paymentService.handleWebhook(webhookBody);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "orderCode", response.getOrderCode(),
                "status", response.getStatus(),
                "message", response.getMessage()
        ));
    }
}
