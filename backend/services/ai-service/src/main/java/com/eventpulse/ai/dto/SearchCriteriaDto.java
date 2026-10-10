package com.eventpulse.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteriaDto {
    private String city;
    private String category;
    private BigDecimal maxPrice;
    private String currency;
    private String keywords;
    private Instant startsAfter;
    private Instant endsBefore;
}
