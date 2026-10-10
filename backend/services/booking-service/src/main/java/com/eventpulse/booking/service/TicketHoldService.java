package com.eventpulse.booking.service;

import java.time.Duration;
import java.util.UUID;

public interface TicketHoldService {

    void recordHold(UUID bookingId, UUID eventId, int quantity, Duration duration);

    void releaseHold(UUID bookingId);

    boolean isHoldActive(UUID bookingId);
}
