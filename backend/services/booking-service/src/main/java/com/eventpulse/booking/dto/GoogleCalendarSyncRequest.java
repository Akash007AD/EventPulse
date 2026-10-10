package com.eventpulse.booking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleCalendarSyncRequest {

    @NotBlank(message = "Google OAuth access token is required to add event to Google Calendar")
    private String googleAccessToken;
}
