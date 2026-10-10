package com.eventpulse.notification.controller;

import com.eventpulse.notification.config.KeycloakJwtAuthenticationConverter;
import com.eventpulse.notification.config.SecurityConfig;
import com.eventpulse.notification.dto.NotificationResponse;
import com.eventpulse.notification.dto.PagedResponse;
import com.eventpulse.notification.entity.NotificationStatus;
import com.eventpulse.notification.entity.NotificationType;
import com.eventpulse.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import({SecurityConfig.class, KeycloakJwtAuthenticationConverter.class})
class NotificationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private com.eventpulse.notification.service.TicketEmailService ticketEmailService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("GET /api/v1/notifications/me without token returns 401 Unauthorized")
    void getMyNotifications_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/me with customer token returns 200 OK")
    void getMyNotifications_authenticated_returns200() throws Exception {
        NotificationResponse item = NotificationResponse.builder()
                .id(UUID.randomUUID())
                .recipientSubject("cust-1")
                .notificationType(NotificationType.BOOKING_CONFIRMATION)
                .title("Booking Confirmed")
                .body("Your booking is confirmed")
                .status(NotificationStatus.SENT)
                .attemptCount(1)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        PagedResponse<NotificationResponse> pagedResponse = PagedResponse.<NotificationResponse>builder()
                .content(List.of(item))
                .pageNumber(0)
                .pageSize(20)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(notificationService.getMyNotifications(eq("cust-1"), anyInt(), anyInt())).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/notifications/me")
                        .with(jwt().jwt(builder -> builder.subject("cust-1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Booking Confirmed"));
    }

    @Test
    @DisplayName("POST /api/v1/notifications/{id}/retry with CUSTOMER role returns 403 Forbidden (Admin only)")
    void retry_customerRole_returns403() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/notifications/" + id + "/retry")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/notifications/{id}/retry with ADMIN role returns 200 OK")
    void retry_adminRole_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        NotificationResponse response = NotificationResponse.builder()
                .id(id)
                .recipientSubject("cust-1")
                .notificationType(NotificationType.BOOKING_CONFIRMATION)
                .title("Booking Confirmed")
                .body("Retried body")
                .status(NotificationStatus.SENT)
                .attemptCount(2)
                .build();

        when(notificationService.retryNotification(id)).thenReturn(response);

        mockMvc.perform(post("/api/v1/notifications/" + id + "/retry")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.attemptCount").value(2));
    }

    @Test
    @DisplayName("POST /api/v1/notifications/tickets/send with CUSTOMER role returns 403 Forbidden")
    void sendTicketEmail_customerRole_returns403() throws Exception {
        String jsonPayload = """
                {
                    "recipientSubject": "cust-1",
                    "recipientEmail": "cust@example.com",
                    "bookingId": "00000000-0000-0000-0000-000000000001",
                    "eventId": "00000000-0000-0000-0000-000000000002",
                    "eventTitle": "Spring AI Summit",
                    "tickets": [
                        {
                            "ticketId": "00000000-0000-0000-0000-000000000003",
                            "ticketNumber": "TKT-001",
                            "qrCodeToken": "EP.token.abc"
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/api/v1/notifications/tickets/send")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(jsonPayload)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/notifications/tickets/send with ORGANIZER role returns 201 Created")
    void sendTicketEmail_organizerRole_returns201() throws Exception {
        String jsonPayload = """
                {
                    "recipientSubject": "cust-1",
                    "recipientEmail": "cust@example.com",
                    "bookingId": "00000000-0000-0000-0000-000000000001",
                    "eventId": "00000000-0000-0000-0000-000000000002",
                    "eventTitle": "Spring AI Summit",
                    "tickets": [
                        {
                            "ticketId": "00000000-0000-0000-0000-000000000003",
                            "ticketNumber": "TKT-001",
                            "qrCodeToken": "EP.token.abc"
                        }
                    ]
                }
                """;

        NotificationResponse response = NotificationResponse.builder()
                .id(UUID.randomUUID())
                .recipientSubject("cust-1")
                .notificationType(NotificationType.TICKET_DELIVERY)
                .title("Your EventPulse Tickets")
                .status(NotificationStatus.SENT)
                .attemptCount(1)
                .build();

        when(ticketEmailService.sendTicketDeliveryEmail(org.mockito.ArgumentMatchers.any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/notifications/tickets/send")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(jsonPayload)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ORGANIZER"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.notificationType").value("TICKET_DELIVERY"));
    }
}
