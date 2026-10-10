package com.eventpulse.booking.service;

import com.eventpulse.booking.dto.CheckInResponse;
import com.eventpulse.booking.dto.ScanQrRequest;
import com.eventpulse.booking.dto.TicketResponse;
import com.eventpulse.booking.entity.Booking;

import java.util.List;
import java.util.UUID;

public interface TicketService {

    /**
     * Issues individual tickets with signed QR tokens for a confirmed booking.
     */
    List<TicketResponse> issueTicketsForBooking(Booking booking);

    /**
     * Retrieves all issued tickets for a specific booking.
     */
    List<TicketResponse> getTicketsForBooking(UUID bookingId, String customerSubject);

    /**
     * Retrieves a single ticket by its UUID.
     */
    TicketResponse getTicketById(UUID ticketId, String customerSubject);

    /**
     * Cancels/invalidates all issued tickets when a booking is cancelled or refunded.
     */
    void cancelTicketsForBooking(UUID bookingId);

    /**
     * Scans and validates an attendee QR code at the event gate.
     * Prevents forgery, double-entry (anti-passback), and admission to wrong events.
     */
    CheckInResponse scanAndCheckIn(ScanQrRequest request, String staffSubject);
}
