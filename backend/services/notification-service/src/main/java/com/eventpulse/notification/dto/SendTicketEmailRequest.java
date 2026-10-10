package com.eventpulse.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendTicketEmailRequest {

    @NotBlank
    private String recipientSubject;

    @NotBlank
    @Email
    private String recipientEmail;

    @NotNull
    private UUID bookingId;

    @NotNull
    private UUID eventId;

    @NotBlank
    private String eventTitle;

    private String venue;

    private String eventDateTime;

    @NotEmpty
    private List<TicketItemDto> tickets;
}
