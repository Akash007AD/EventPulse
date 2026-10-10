package com.eventpulse.booking.controller;

import com.eventpulse.booking.config.KeycloakJwtAuthenticationConverter;
import com.eventpulse.booking.config.SecurityConfig;
import com.eventpulse.booking.dto.CheckInResponse;
import com.eventpulse.booking.dto.CheckInResult;
import com.eventpulse.booking.dto.ScanQrRequest;
import com.eventpulse.booking.dto.TicketResponse;
import com.eventpulse.booking.entity.TicketStatus;
import com.eventpulse.booking.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
@Import({SecurityConfig.class, KeycloakJwtAuthenticationConverter.class})
class TicketControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("POST /api/v1/tickets/scan without authentication returns 401 ProblemDetail")
    void scan_unauthenticated_returns401() throws Exception {
        ScanQrRequest request = ScanQrRequest.builder()
                .qrCodeToken("EP.test.token")
                .eventId(UUID.randomUUID())
                .build();

        mockMvc.perform(post("/api/v1/tickets/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("POST /api/v1/tickets/scan with CUSTOMER role returns 403 Forbidden")
    void scan_customerRole_returns403() throws Exception {
        ScanQrRequest request = ScanQrRequest.builder()
                .qrCodeToken("EP.test.token")
                .eventId(UUID.randomUUID())
                .build();

        mockMvc.perform(post("/api/v1/tickets/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("POST /api/v1/tickets/scan with ORGANIZER role returns 200 OK")
    void scan_organizerRole_returns200() throws Exception {
        UUID eventId = UUID.randomUUID();
        ScanQrRequest request = ScanQrRequest.builder()
                .qrCodeToken("EP.test.token")
                .eventId(eventId)
                .gateLocation("East Gate")
                .build();

        CheckInResponse response = CheckInResponse.builder()
                .result(CheckInResult.ALLOW_ENTRY)
                .message("Entry permitted")
                .valid(true)
                .scanTimestamp(Instant.now())
                .ticket(TicketResponse.builder()
                        .id(UUID.randomUUID())
                        .ticketNumber("TKT-001")
                        .eventId(eventId)
                        .customerSubject("cust-bob")
                        .status(TicketStatus.CHECKED_IN)
                        .checkedInAt(Instant.now())
                        .checkedInBy("org-john")
                        .build())
                .build();

        when(ticketService.scanAndCheckIn(any(ScanQrRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/tickets/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt().jwt(j -> j.claim("preferred_username", "org-john"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ORGANIZER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("ALLOW_ENTRY"))
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/tickets/{id} by owning customer returns 200 OK")
    void getTicket_owningCustomer_returns200() throws Exception {
        UUID ticketId = UUID.randomUUID();
        TicketResponse ticket = TicketResponse.builder()
                .id(ticketId)
                .bookingId(UUID.randomUUID())
                .eventId(UUID.randomUUID())
                .customerSubject("cust-alice")
                .ticketNumber("TKT-001")
                .qrCodeToken("EP.token")
                .status(TicketStatus.ISSUED)
                .build();

        when(ticketService.getTicketById(eq(ticketId), eq("cust-alice"))).thenReturn(ticket);

        mockMvc.perform(get("/api/v1/tickets/" + ticketId)
                        .with(jwt().jwt(j -> j.subject("cust-alice"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketNumber").value("TKT-001"));
    }

    @Test
    @DisplayName("GET /api/v1/tickets/{id} by different customer returns 403 Forbidden")
    void getTicket_differentCustomer_returns403() throws Exception {
        UUID ticketId = UUID.randomUUID();

        when(ticketService.getTicketById(eq(ticketId), eq("cust-intruder")))
                .thenThrow(new org.springframework.security.access.AccessDeniedException("You do not have permission to view this ticket."));

        mockMvc.perform(get("/api/v1/tickets/" + ticketId)
                        .with(jwt().jwt(j -> j.subject("cust-intruder"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isForbidden());
    }
}
