package com.eventpulse.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailsDto {

    private UUID id;
    private UUID eventId;
    private String customerSubject;
    private Integer quantity;
    private BigDecimal amount;
    private String currency;
    private String status;
    private Instant expiresAt;
}
