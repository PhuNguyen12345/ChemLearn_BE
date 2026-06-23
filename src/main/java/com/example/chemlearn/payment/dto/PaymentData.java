package com.example.chemlearn.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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
public class PaymentData {
	private Long orderCode;

	private java.util.UUID userId;

	@NotBlank
	private String packageCode;

	@Positive
	private Long amount;

	@NotBlank
	private String description;

	@NotBlank
	private String returnUrl;

	@NotBlank
	private String cancelUrl;

	private String buyerName;
	private String buyerEmail;
	private String buyerPhone;
	private Long expiredAt;
}
