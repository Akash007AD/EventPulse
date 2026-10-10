package com.eventpulse.payment.dto;

import com.eventpulse.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderResponse {

    private UUID paymentId;
    private UUID bookingId;
    private String razorpayOrderId;
    private BigDecimal amount;
    private String currency;
    private String keyId;
    private PaymentStatus status;
}
