package com.eventpulse.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformOverviewResponse {

    private BigDecimal totalGrossRevenue;
    private String currency;
    private Long totalTicketsSold;
    private Long totalConfirmedBookings;
    private Long totalTrackedEvents;
}
