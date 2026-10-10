package com.eventpulse.booking.service;

import com.eventpulse.booking.dto.GoogleCalendarEventResponse;

import java.util.UUID;

public interface GoogleCalendarService {

    /**
     * Directly inserts a booked event into the user's primary Google Calendar
     * using the Google OAuth access token obtained during login/signup.
     */
    GoogleCalendarEventResponse syncBookingToGoogleCalendar(UUID bookingId, String googleAccessToken, String customerSubject);

    /**
     * Generates universal RFC 5545 .ics text for a booking.
     */
    String generateIcsCalendar(UUID bookingId, String customerSubject);
}
