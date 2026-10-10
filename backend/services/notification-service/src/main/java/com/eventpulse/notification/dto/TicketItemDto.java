package com.eventpulse.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketItemDto {
    private UUID ticketId;
    private String ticketNumber;
    private String qrCodeToken;
    private String eventTitle;
    private String seatInfo;
}
