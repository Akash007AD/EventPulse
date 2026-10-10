package com.eventpulse.notification.service;

import com.eventpulse.notification.dto.NotificationPayload;
import com.eventpulse.notification.dto.NotificationResponse;
import com.eventpulse.notification.dto.SendTicketEmailRequest;
import com.eventpulse.notification.dto.TicketItemDto;
import com.eventpulse.notification.entity.NotificationStatus;
import com.eventpulse.notification.entity.NotificationType;
import com.eventpulse.notification.service.impl.TicketEmailServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketEmailServiceTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TicketEmailServiceImpl ticketEmailService;

    @Test
    @DisplayName("sendTicketDeliveryEmail constructs rich HTML with QR tokens and delegates to notificationService")
    void sendTicketDeliveryEmail_success() {
        UUID bookingId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();

        TicketItemDto ticket = TicketItemDto.builder()
                .ticketId(ticketId)
                .ticketNumber("TKT-20261010-001")
                .qrCodeToken("EP." + ticketId + "." + bookingId + "." + eventId + ".1790000000.abc123hash")
                .eventTitle("Global Cloud AI Summit")
                .seatInfo("VIP-12")
                .build();

        SendTicketEmailRequest request = SendTicketEmailRequest.builder()
                .recipientSubject("cust-alice")
                .recipientEmail("alice@example.com")
                .bookingId(bookingId)
                .eventId(eventId)
                .eventTitle("Global Cloud AI Summit")
                .venue("Bangalore International Convention Centre")
                .tickets(List.of(ticket))
                .build();

        NotificationResponse mockResponse = NotificationResponse.builder()
                .id(UUID.randomUUID())
                .recipientSubject("cust-alice")
                .recipientEmail("alice@example.com")
                .notificationType(NotificationType.TICKET_DELIVERY)
                .title("Your EventPulse Tickets")
                .status(NotificationStatus.SENT)
                .attemptCount(1)
                .createdAt(Instant.now())
                .build();

        when(notificationService.sendNotification(any(NotificationPayload.class))).thenReturn(mockResponse);

        NotificationResponse response = ticketEmailService.sendTicketDeliveryEmail(request);

        assertThat(response).isNotNull();
        assertThat(response.getNotificationType()).isEqualTo(NotificationType.TICKET_DELIVERY);

        ArgumentCaptor<NotificationPayload> payloadCaptor = ArgumentCaptor.forClass(NotificationPayload.class);
        verify(notificationService).sendNotification(payloadCaptor.capture());

        NotificationPayload captured = payloadCaptor.getValue();
        assertThat(captured.getRecipientEmail()).isEqualTo("alice@example.com");
        assertThat(captured.getNotificationType()).isEqualTo(NotificationType.TICKET_DELIVERY);
        assertThat(captured.getTitle()).contains("Global Cloud AI Summit");
        assertThat(captured.getBody()).contains("TKT-20261010-001");
        assertThat(captured.getBody()).contains("Bangalore International Convention Centre");
        assertThat(captured.getBody()).contains("Single-Use Security Notice");
        assertThat(captured.getBody()).contains("EP." + ticketId);
    }
}
