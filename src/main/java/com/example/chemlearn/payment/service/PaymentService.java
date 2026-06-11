package com.example.chemlearn.payment.service;

import java.util.Map;

import com.example.chemlearn.payment.dto.CheckoutResponseData;
import com.example.chemlearn.payment.dto.PaymentData;

public interface PaymentService {
    CheckoutResponseData createPaymentLink(PaymentData request);

    CheckoutResponseData getPaymentStatus(Long orderCode);

    CheckoutResponseData handleWebhook(Map<String, Object> webhookBody);
}