package com.eventpulse.booking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanQrRequest {

    @NotBlank(message = "QR code token is required")
    private String qrCodeToken;

    /**
     * Optional target event ID to ensure ticket is scanned at the correct event entrance.
     */
    private UUID eventId;

    /**
     * Optional gate name or terminal location (e.g. "Gate 3 North").
     */
    private String gateLocation;
}
