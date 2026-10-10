package com.eventpulse.ai.dto;

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
public class EventSummaryDto {
    private UUID id;
    private String title;
    private String description;
    private String category;
    private String venue;
    private String city;
    private Instant startsAt;
    private Instant endsAt;
    private BigDecimal basePrice;
    private String currency;
    private Integer availableTickets;
    private String status;
}
