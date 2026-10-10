package com.eventpulse.booking.exception;

public class ConcurrentBookingException extends RuntimeException {
    public ConcurrentBookingException(String message) {
        super(message);
    }
}
