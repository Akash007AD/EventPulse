package com.eventpulse.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleCalendarEventResponse {

    private UUID bookingId;
    private String googleEventId;
    private String htmlLink;
    private String status;
    private String summary;
    private String message;
}
