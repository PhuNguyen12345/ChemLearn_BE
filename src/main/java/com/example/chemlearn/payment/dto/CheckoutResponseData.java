package com.example.chemlearn.payment.dto;

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
public class CheckoutResponseData {
	private Long orderCode;
	private java.util.UUID userId;
	private String packageCode;
	private Long amount;
	private String status;
	private String paymentLinkId;
	private String checkoutUrl;
	private String qrCode;
	private String message;
}
