package com.eventpulse.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPaymentRequest {

    @NotBlank(message = "razorpayOrderId is mandatory")
    private String razorpayOrderId;

    @NotBlank(message = "razorpayPaymentId is mandatory")
    private String razorpayPaymentId;

    @NotBlank(message = "razorpaySignature is mandatory")
    private String razorpaySignature;
}
