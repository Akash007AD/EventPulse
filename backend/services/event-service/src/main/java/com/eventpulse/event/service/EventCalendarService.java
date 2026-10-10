package com.eventpulse.event.service;

import com.eventpulse.event.dto.EventResponse;

import java.util.UUID;

public interface EventCalendarService {

    /**
     * Generates standard RFC 5545 iCalendar (.ics) string for an event.
     */
    String generateIcsCalendar(UUID eventId);

    /**
     * Generates a direct Google Calendar Web Template link.
     */
    String generateGoogleCalendarUrl(UUID eventId);
}
