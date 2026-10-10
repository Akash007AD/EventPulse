package com.eventpulse.booking.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class BookingNotFoundException extends RuntimeException {

    private final UUID bookingId;

    public BookingNotFoundException(UUID bookingId) {
        super("Booking not found with ID: " + bookingId);
        this.bookingId = bookingId;
    }
}
