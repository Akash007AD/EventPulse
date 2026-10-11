package com.eventpulse.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostCalculationRequest {
    private UUID eventId;
    private Integer quantity;
    private List<SeatCostItem> seats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatCostItem {
        private String id;
        private String row;
        private Integer number;
        private String tier;
        private BigDecimal price;
    }
}
