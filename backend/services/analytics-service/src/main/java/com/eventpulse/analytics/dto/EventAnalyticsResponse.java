package com.eventpulse.analytics.dto;

import com.eventpulse.analytics.entity.EventAnalytics;
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
public class EventAnalyticsResponse {

    private UUID eventId;
    private String eventTitle;
    private BigDecimal totalRevenue;
    private String currency;
    private Integer ticketsSold;
    private Integer confirmedBookings;
    private Instant lastUpdated;

    public static EventAnalyticsResponse fromEntity(EventAnalytics entity) {
        return EventAnalyticsResponse.builder()
                .eventId(entity.getEventId())
                .eventTitle(entity.getEventTitle())
                .totalRevenue(entity.getTotalRevenue())
                .currency(entity.getCurrency())
                .ticketsSold(entity.getTicketsSold())
                .confirmedBookings(entity.getConfirmedBookings())
                .lastUpdated(entity.getUpdatedAt())
                .build();
    }
}
