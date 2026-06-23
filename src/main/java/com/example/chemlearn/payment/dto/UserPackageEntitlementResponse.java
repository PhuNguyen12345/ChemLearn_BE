package com.example.chemlearn.payment.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPackageEntitlementResponse {
    private UUID id;
    private UUID userId;
    private String packageCode;
    private UUID paymentTransactionId;
    private String status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private String metadataJson;
}
