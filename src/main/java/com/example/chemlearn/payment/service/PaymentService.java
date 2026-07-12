package com.example.chemlearn.payment.service;

import java.util.Map;
import java.util.List;

import com.example.chemlearn.payment.dto.CheckoutResponseData;
import com.example.chemlearn.payment.dto.LearningPackageResponse;
import com.example.chemlearn.payment.dto.PaymentData;
import com.example.chemlearn.payment.dto.UserPackageEntitlementResponse;

public interface PaymentService {
    CheckoutResponseData createPaymentLink(PaymentData request);

    CheckoutResponseData getPaymentStatus(Long orderCode);

    CheckoutResponseData handleWebhook(Map<String, Object> webhookBody);

    List<LearningPackageResponse> getActivePackages();

    LearningPackageResponse getPackage(String packageCode);

    List<UserPackageEntitlementResponse> getCurrentUserEntitlements();

    UserPackageEntitlementResponse getCurrentUserEntitlement(String packageCode);

}
