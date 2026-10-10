package com.eventpulse.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInResponse {

    private boolean valid;
    private CheckInResult result;
    private String message;
    private Instant scanTimestamp;
    private TicketResponse ticket;
}
