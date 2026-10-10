package com.eventpulse.booking.controller;

import com.eventpulse.booking.config.KeycloakJwtAuthenticationConverter;
import com.eventpulse.booking.config.SecurityConfig;
import com.eventpulse.booking.dto.BookingResponse;
import com.eventpulse.booking.dto.CreateBookingRequest;
import com.eventpulse.booking.entity.BookingStatus;
import com.eventpulse.booking.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
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

@WebMvcTest(BookingController.class)
@Import({SecurityConfig.class, KeycloakJwtAuthenticationConverter.class})
class BookingSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private com.eventpulse.booking.service.GoogleCalendarService calendarService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("POST /api/v1/bookings without authentication returns 401 ProblemDetail")
    void createBooking_unauthenticated_returns401() throws Exception {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .eventId(UUID.randomUUID())
                .quantity(2)
                .amount(new BigDecimal("1000.00"))
                .currency("INR")
                .build();

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("POST /api/v1/bookings with CUSTOMER role returns 201 Created and sets customerSubject")
    void createBooking_withCustomerRole_returns201() throws Exception {
        UUID eventId = UUID.randomUUID();
        CreateBookingRequest request = CreateBookingRequest.builder()
                .eventId(eventId)
                .quantity(2)
                .amount(new BigDecimal("1000.00"))
                .currency("INR")
                .build();

        UUID bookingId = UUID.randomUUID();
        BookingResponse response = BookingResponse.builder()
                .id(bookingId)
                .eventId(eventId)
                .customerSubject("cust-user-1")
                .quantity(2)
                .amount(new BigDecimal("1000.00"))
                .currency("INR")
                .status(BookingStatus.PENDING_PAYMENT)
                .expiresAt(Instant.now().plusSeconds(900))
                .build();

        when(bookingService.createBooking(any(CreateBookingRequest.class), eq("cust-user-1")))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/bookings")
                        .with(jwt().jwt(jwt -> jwt.subject("cust-user-1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(bookingId.toString()))
                .andExpect(jsonPath("$.customerSubject").value("cust-user-1"));
    }

    @Test
    @DisplayName("GET /api/v1/bookings/{id} by non-owner returns 403 Forbidden ProblemDetail")
    void getBookingById_byNonOwner_returns403() throws Exception {
        UUID bookingId = UUID.randomUUID();

        when(bookingService.getBookingById(eq(bookingId), eq("cust-hacker")))
                .thenThrow(new AccessDeniedException("You do not have permission to access this booking."));

        mockMvc.perform(get("/api/v1/bookings/{id}", bookingId)
                        .with(jwt().jwt(jwt -> jwt.subject("cust-hacker"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("GET /api/v1/bookings/{id} by ADMIN succeeds regardless of owner")
    void getBookingById_byAdmin_succeeds() throws Exception {
        UUID bookingId = UUID.randomUUID();
        BookingResponse response = BookingResponse.builder()
                .id(bookingId)
                .eventId(UUID.randomUUID())
                .customerSubject("cust-owner")
                .quantity(1)
                .amount(new BigDecimal("500.00"))
                .currency("INR")
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingService.getBookingById(eq(bookingId), eq(null)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/bookings/{id}", bookingId)
                        .with(jwt().jwt(jwt -> jwt.subject("admin-user"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId.toString()));
    }
}
