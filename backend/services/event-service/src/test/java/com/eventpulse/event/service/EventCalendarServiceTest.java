package com.eventpulse.event.service;

import com.eventpulse.event.dto.EventResponse;
import com.eventpulse.event.entity.EventStatus;
import com.eventpulse.event.service.impl.EventCalendarServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventCalendarServiceTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventCalendarServiceImpl calendarService;

    @Test
    @DisplayName("generateIcsCalendar generates compliant RFC 5545 VCALENDAR payload")
    void generateIcsCalendar_success() {
        UUID eventId = UUID.randomUUID();
        Instant startsAt = Instant.parse("2026-11-16T19:30:00Z");
        Instant endsAt = startsAt.plus(3, ChronoUnit.HOURS);

        EventResponse event = EventResponse.builder()
                .id(eventId)
                .name("London Symphony Orchestra: Hans Zimmer Live")
                .description("Iconic soundtrack symphonic masterpieces.")
                .venueName("Royal Albert Hall")
                .city("London")
                .startsAt(startsAt)
                .endsAt(endsAt)
                .currency("GBP")
                .status(EventStatus.PUBLISHED)
                .build();

        when(eventService.getPublishedEventById(eventId)).thenReturn(event);

        String ics = calendarService.generateIcsCalendar(eventId);

        assertThat(ics).contains("BEGIN:VCALENDAR");
        assertThat(ics).contains("VERSION:2.0");
        assertThat(ics).contains("BEGIN:VEVENT");
        assertThat(ics).contains("SUMMARY:London Symphony Orchestra: Hans Zimmer Live");
        assertThat(ics).contains("LOCATION:Royal Albert Hall\\, London");
        assertThat(ics).contains("DTSTART:20261116T193000Z");
        assertThat(ics).contains("DTEND:20261116T223000Z");
        assertThat(ics).contains("END:VEVENT");
        assertThat(ics).contains("END:VCALENDAR");
    }

    @Test
    @DisplayName("generateGoogleCalendarUrl generates encoded URL with parameters")
    void generateGoogleCalendarUrl_success() {
        UUID eventId = UUID.randomUUID();
        Instant startsAt = Instant.parse("2026-11-16T19:30:00Z");
        Instant endsAt = startsAt.plus(3, ChronoUnit.HOURS);

        EventResponse event = EventResponse.builder()
                .id(eventId)
                .name("London Symphony Orchestra")
                .venueName("Royal Albert Hall")
                .city("London")
                .startsAt(startsAt)
                .endsAt(endsAt)
                .build();

        when(eventService.getPublishedEventById(eventId)).thenReturn(event);

        String url = calendarService.generateGoogleCalendarUrl(eventId);

        assertThat(url).startsWith("https://calendar.google.com/calendar/render?action=TEMPLATE");
        assertThat(url).contains("London+Symphony+Orchestra");
        assertThat(url).contains("20261116T193000Z/20261116T223000Z");
    }
}
