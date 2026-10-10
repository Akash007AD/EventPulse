package com.eventpulse.booking.dto;

import com.eventpulse.booking.entity.Ticket;
import com.eventpulse.booking.entity.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    private UUID id;
    private UUID bookingId;
    private UUID eventId;
    private String customerSubject;
    private String ticketNumber;
    private String qrCodeToken;
    private TicketStatus status;
    private Instant checkedInAt;
    private String checkedInBy;
    private Instant createdAt;

    public static TicketResponse fromEntity(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .bookingId(ticket.getBookingId())
                .eventId(ticket.getEventId())
                .customerSubject(ticket.getCustomerSubject())
                .ticketNumber(ticket.getTicketNumber())
                .qrCodeToken(ticket.getQrCodeToken())
                .status(ticket.getStatus())
                .checkedInAt(ticket.getCheckedInAt())
                .checkedInBy(ticket.getCheckedInBy())
                .createdAt(ticket.getCreatedAt())
                .build();
    }
}
