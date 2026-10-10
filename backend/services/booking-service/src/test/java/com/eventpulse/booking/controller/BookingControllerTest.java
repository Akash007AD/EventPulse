package com.eventpulse.booking.controller;

import com.eventpulse.booking.dto.BookingResponse;
import com.eventpulse.booking.dto.CreateBookingRequest;
import com.eventpulse.booking.entity.BookingStatus;
import com.eventpulse.booking.exception.BookingNotFoundException;
import com.eventpulse.booking.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private com.eventpulse.booking.service.GoogleCalendarService calendarService;

    @Test
    @DisplayName("POST /api/v1/bookings should return 201 Created on valid request")
    void createBooking_shouldReturn201() throws Exception {
        UUID bookingId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        CreateBookingRequest request = new CreateBookingRequest(
                eventId, 2, new BigDecimal("499.00"), "INR", "cust-1"
        );

        BookingResponse response = new BookingResponse(
                bookingId, "cust-1", eventId, 2, new BigDecimal("499.00"), "INR",
                BookingStatus.PENDING_PAYMENT, Instant.now().plusSeconds(900),
                Instant.now(), Instant.now()
        );

        when(bookingService.createBooking(any(CreateBookingRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(bookingId.toString())))
                .andExpect(jsonPath("$.status", is("PENDING_PAYMENT")))
                .andExpect(jsonPath("$.quantity", is(2)));
    }

    @Test
    @DisplayName("POST /api/v1/bookings should return 400 Bad Request when quantity is 0")
    void createBooking_shouldReturn400WhenQuantityZero() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest(
                UUID.randomUUID(), 0, new BigDecimal("499.00"), "INR", "cust-1"
        );

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.invalidParams.quantity", containsString("at least 1")));
    }

    @Test
    @DisplayName("POST /api/v1/bookings should return 400 Bad Request when quantity exceeds 10")
    void createBooking_shouldReturn400WhenQuantityExceeds10() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest(
                UUID.randomUUID(), 11, new BigDecimal("499.00"), "INR", "cust-1"
        );

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.invalidParams.quantity", containsString("Cannot book more than 10")));
    }

    @Test
    @DisplayName("GET /api/v1/bookings/{id} should return 404 Not Found when missing")
    void getBookingById_shouldReturn404() throws Exception {
        UUID bookingId = UUID.randomUUID();
        when(bookingService.getBookingById(eq(bookingId), any())).thenThrow(new BookingNotFoundException(bookingId));

        mockMvc.perform(get("/api/v1/bookings/{id}", bookingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("BOOKING_NOT_FOUND")));
    }

    @Test
    @DisplayName("POST /api/v1/bookings/{id}/cancel should return 200 OK with CANCELLED status")
    void cancelBooking_shouldReturn200() throws Exception {
        UUID bookingId = UUID.randomUUID();
        BookingResponse cancelled = new BookingResponse(
                bookingId, "cust-1", UUID.randomUUID(), 1, new BigDecimal("200.00"), "INR",
                BookingStatus.CANCELLED, Instant.now().plusSeconds(900), Instant.now(), Instant.now()
        );

        when(bookingService.cancelBooking(eq(bookingId), any())).thenReturn(cancelled);

        mockMvc.perform(post("/api/v1/bookings/{id}/cancel", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }
}
