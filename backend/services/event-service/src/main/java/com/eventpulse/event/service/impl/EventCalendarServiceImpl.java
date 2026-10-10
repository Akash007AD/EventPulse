package com.eventpulse.event.service.impl;

import com.eventpulse.event.dto.EventResponse;
import com.eventpulse.event.service.EventCalendarService;
import com.eventpulse.event.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventCalendarServiceImpl implements EventCalendarService {

    private final EventService eventService;

    private static final DateTimeFormatter ICS_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    @Override
    public String generateIcsCalendar(UUID eventId) {
        log.info("Generating .ics calendar entry for event: {}", eventId);
        EventResponse event = eventService.getPublishedEventById(eventId);

        String dtStamp = ICS_DATE_TIME_FORMATTER.format(Instant.now());
        String dtStart = ICS_DATE_TIME_FORMATTER.format(event.getStartsAt());
        String dtEnd = ICS_DATE_TIME_FORMATTER.format(event.getEndsAt());
        String uid = "event-" + event.getId() + "@eventpulse.io";

        String summary = escapeIcsText(event.getName());
        String location = escapeIcsText(event.getVenueName() + ", " + event.getCity());
        String description = escapeIcsText(event.getDescription() != null ? event.getDescription() : "Event tickets booked via EventPulse.");

        return "BEGIN:VCALENDAR\r\n" +
                "VERSION:2.0\r\n" +
                "PRODID:-//EventPulse//Ticketing Platform//EN\r\n" +
                "CALSCALE:GREGORIAN\r\n" +
                "METHOD:PUBLISH\r\n" +
                "BEGIN:VEVENT\r\n" +
                "UID:" + uid + "\r\n" +
                "DTSTAMP:" + dtStamp + "\r\n" +
                "DTSTART:" + dtStart + "\r\n" +
                "DTEND:" + dtEnd + "\r\n" +
                "SUMMARY:" + summary + "\r\n" +
                "DESCRIPTION:" + description + "\r\n" +
                "LOCATION:" + location + "\r\n" +
                "STATUS:CONFIRMED\r\n" +
                "END:VEVENT\r\n" +
                "END:VCALENDAR\r\n";
    }

    @Override
    public String generateGoogleCalendarUrl(UUID eventId) {
        EventResponse event = eventService.getPublishedEventById(eventId);

        String dtStart = ICS_DATE_TIME_FORMATTER.format(event.getStartsAt());
        String dtEnd = ICS_DATE_TIME_FORMATTER.format(event.getEndsAt());

        String details = (event.getDescription() != null ? event.getDescription() : "") +
                "\n\nVenue: " + event.getVenueName() + ", " + event.getCity() +
                "\nBooked via EventPulse";

        return String.format(
                "https://calendar.google.com/calendar/render?action=TEMPLATE&text=%s&dates=%s/%s&details=%s&location=%s",
                urlEncode(event.getName()),
                dtStart,
                dtEnd,
                urlEncode(details),
                urlEncode(event.getVenueName() + ", " + event.getCity())
        );
    }

    private String escapeIcsText(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\n", "\\n");
    }

    private String urlEncode(String text) {
        if (text == null) return "";
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}
