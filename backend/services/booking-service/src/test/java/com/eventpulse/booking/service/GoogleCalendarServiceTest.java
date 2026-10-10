package com.eventpulse.booking.service;

import com.eventpulse.booking.client.EventCatalogClient;
import com.eventpulse.booking.dto.EventSummaryDto;
import com.eventpulse.booking.dto.GoogleCalendarEventResponse;
import com.eventpulse.booking.entity.Booking;
import com.eventpulse.booking.entity.BookingStatus;
import com.eventpulse.booking.repository.BookingRepository;
import com.eventpulse.booking.service.impl.GoogleCalendarServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleCalendarServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EventCatalogClient eventCatalogClient;

    @InjectMocks
    private GoogleCalendarServiceImpl googleCalendarService;

    @Test
    @DisplayName("generateIcsCalendar generates compliant RFC 5545 calendar with seat numbers")
    void generateIcsCalendar_success() {
        UUID bookingId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Booking booking = Booking.builder()
                .id(bookingId)
                .eventId(eventId)
                .customerSubject("cust-alice")
                .quantity(2)
                .amount(new BigDecimal("170.00"))
                .currency("USD")
                .seatNumbers("Row C Seat 14, Row C Seat 15")
                .status(BookingStatus.CONFIRMED)
                .build();

        EventSummaryDto event = EventSummaryDto.builder()
                .id(eventId)
                .name("London Symphony Orchestra: Hans Zimmer Live")
                .venueName("Royal Albert Hall")
                .city("London")
                .startsAt(Instant.parse("2026-11-16T19:30:00Z"))
                .endsAt(Instant.parse("2026-11-16T22:30:00Z"))
                .status("PUBLISHED")
                .currency("USD")
                .build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(eventCatalogClient.getPublishedEvent(eventId)).thenReturn(Optional.of(event));

        String ics = googleCalendarService.generateIcsCalendar(bookingId, "cust-alice");

        assertThat(ics).contains("BEGIN:VCALENDAR");
        assertThat(ics).contains("BEGIN:VEVENT");
        assertThat(ics).contains("SUMMARY:EventPulse: London Symphony Orchestra: Hans Zimmer Live");
        assertThat(ics).contains("Row C Seat 14\\, Row C Seat 15");
        assertThat(ics).contains("DTSTART:20261116T193000Z");
        assertThat(ics).contains("DTEND:20261116T223000Z");
        assertThat(ics).contains("END:VEVENT");
    }

    @Test
    @DisplayName("generateIcsCalendar throws AccessDeniedException when customer subject does not match")
    void generateIcsCalendar_forbidden_throwsAccessDenied() {
        UUID bookingId = UUID.randomUUID();
        Booking booking = Booking.builder()
                .id(bookingId)
                .customerSubject("cust-alice")
                .build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> googleCalendarService.generateIcsCalendar(bookingId, "cust-intruder"))
                .isInstanceOf(AccessDeniedException.class);
    }
}
