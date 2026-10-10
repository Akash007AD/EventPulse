package com.eventpulse.booking.service.impl;

import com.eventpulse.booking.client.EventCatalogClient;
import com.eventpulse.booking.dto.EventSummaryDto;
import com.eventpulse.booking.dto.GoogleCalendarEventResponse;
import com.eventpulse.booking.entity.Booking;
import com.eventpulse.booking.exception.BookingNotFoundException;
import com.eventpulse.booking.repository.BookingRepository;
import com.eventpulse.booking.service.GoogleCalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleCalendarServiceImpl implements GoogleCalendarService {

    private final BookingRepository bookingRepository;
    private final EventCatalogClient eventCatalogClient;

    private static final String GOOGLE_CALENDAR_API_URL = "https://www.googleapis.com/calendar/v3/calendars/primary/events";
    private static final DateTimeFormatter ICS_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    @Override
    public GoogleCalendarEventResponse syncBookingToGoogleCalendar(UUID bookingId, String googleAccessToken, String customerSubject) {
        log.info("Directly syncing booking {} to user's Google Calendar via OAuth token", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (customerSubject != null && !customerSubject.isBlank() && !customerSubject.equals(booking.getCustomerSubject())) {
            throw new AccessDeniedException("You do not have permission to sync this booking to Google Calendar.");
        }

        EventSummaryDto event = eventCatalogClient.getPublishedEvent(booking.getEventId())
                .orElseThrow(() -> new IllegalStateException("Event not found for booking: " + booking.getEventId()));

        String summary = "EventPulse: " + event.getName();
        String seats = (booking.getSeatNumbers() != null && !booking.getSeatNumbers().isBlank())
                ? booking.getSeatNumbers()
                : "General Admission";

        String description = String.format("Confirmed Event Booking via EventPulse\nBooking Reference: %s\nTickets: %d\nSeats: %s\n\nPresent your digital QR pass at the entrance gate for admission.",
                booking.getId(), booking.getQuantity(), seats);

        String location = (event.getVenueName() != null ? event.getVenueName() : "") +
                (event.getCity() != null ? ", " + event.getCity() : "");

        Instant startInstant = event.getStartsAt() != null ? event.getStartsAt() : Instant.now().plusSeconds(86400);
        Instant endInstant = event.getEndsAt() != null ? event.getEndsAt() : startInstant.plusSeconds(7200);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("summary", summary);
        requestBody.put("description", description);
        requestBody.put("location", location);
        requestBody.put("start", Map.of("dateTime", startInstant.toString(), "timeZone", "UTC"));
        requestBody.put("end", Map.of("dateTime", endInstant.toString(), "timeZone", "UTC"));
        requestBody.put("reminders", Map.of(
                "useDefault", false,
                "overrides", List.of(
                        Map.of("method", "popup", "minutes", 120),
                        Map.of("method", "email", "minutes", 1440)
                )
        ));

        try {
            RestClient restClient = RestClient.create();
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri(GOOGLE_CALENDAR_API_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + googleAccessToken.trim())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            String googleEventId = response != null && response.get("id") != null ? response.get("id").toString() : UUID.randomUUID().toString();
            String htmlLink = response != null && response.get("htmlLink") != null ? response.get("htmlLink").toString() : null;
            String status = response != null && response.get("status") != null ? response.get("status").toString() : "confirmed";

            log.info("Successfully added booking {} to Google Calendar. Google Event ID: {}", bookingId, googleEventId);
            return GoogleCalendarEventResponse.builder()
                    .bookingId(bookingId)
                    .googleEventId(googleEventId)
                    .htmlLink(htmlLink)
                    .status(status)
                    .summary(summary)
                    .message("Event successfully added to your Google Calendar.")
                    .build();

        } catch (Exception ex) {
            log.error("Google Calendar API call failed for booking {}: {}", bookingId, ex.getMessage());
            // Return informative response rather than crashing
            return GoogleCalendarEventResponse.builder()
                    .bookingId(bookingId)
                    .status("FAILED")
                    .summary(summary)
                    .message("Google Calendar sync error: " + ex.getMessage())
                    .build();
        }
    }

    @Override
    public String generateIcsCalendar(UUID bookingId, String customerSubject) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (customerSubject != null && !customerSubject.isBlank() && !customerSubject.equals(booking.getCustomerSubject())) {
            throw new AccessDeniedException("You do not have permission to download this calendar pass.");
        }

        EventSummaryDto event = eventCatalogClient.getPublishedEvent(booking.getEventId())
                .orElseThrow(() -> new IllegalStateException("Event not found for booking: " + booking.getEventId()));

        String dtStamp = ICS_DATE_TIME_FORMATTER.format(Instant.now());
        Instant startInstant = event.getStartsAt() != null ? event.getStartsAt() : Instant.now().plusSeconds(86400);
        Instant endInstant = event.getEndsAt() != null ? event.getEndsAt() : startInstant.plusSeconds(7200);

        String dtStart = ICS_DATE_TIME_FORMATTER.format(startInstant);
        String dtEnd = ICS_DATE_TIME_FORMATTER.format(endInstant);
        String uid = "booking-" + booking.getId() + "@eventpulse.io";

        String seats = (booking.getSeatNumbers() != null && !booking.getSeatNumbers().isBlank())
                ? booking.getSeatNumbers()
                : "General Admission";

        String summary = "EventPulse: " + escapeIcsText(event.getName());
        String location = escapeIcsText((event.getVenueName() != null ? event.getVenueName() : "") +
                (event.getCity() != null ? ", " + event.getCity() : ""));
        String description = escapeIcsText(String.format("Booking #%s | Tickets: %d | Seats: %s | Anti-passback QR pass valid for entry.",
                booking.getId(), booking.getQuantity(), seats));

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

    private String escapeIcsText(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\n", "\\n");
    }
}
