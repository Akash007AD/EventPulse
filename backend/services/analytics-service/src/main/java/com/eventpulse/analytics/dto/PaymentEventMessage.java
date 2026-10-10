package com.eventpulse.analytics.dto;

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
public class PaymentEventMessage {

    private String eventId;
    private String eventType;
    private UUID bookingId;
    private UUID paymentId;
    private UUID catalogEventId;
    private BigDecimal amount;
    private String currency;
    private Integer quantity;
}
