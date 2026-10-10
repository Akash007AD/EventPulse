package com.eventpulse.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventSummaryDto {
    private UUID id;
    private String name;
    private String status;
    private String currency;
    private String venueName;
    private String city;
    private java.time.Instant startsAt;
    private java.time.Instant endsAt;

    public EventSummaryDto(UUID id, String name, String status, String currency) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.currency = currency;
    }
}
