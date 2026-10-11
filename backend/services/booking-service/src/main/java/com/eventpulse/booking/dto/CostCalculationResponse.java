package com.eventpulse.booking.dto;

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
public class CostCalculationResponse {
    private UUID eventId;
    private Integer ticketCount;
    private BigDecimal ticketSubtotal;
    private BigDecimal platformFeePerTicket;
    private BigDecimal platformFeeTotal;
    private BigDecimal cgstRatePercent;
    private BigDecimal cgstAmount;
    private BigDecimal sgstRatePercent;
    private BigDecimal sgstAmount;
    private BigDecimal totalGstAmount;
    private BigDecimal grandTotal;
    private String currency;
    private String calculatedBy;
    private Instant calculatedAt;
}
