package com.eventpulse.payment.client;

import com.eventpulse.payment.dto.BookingDetailsDto;

import java.util.Optional;
import java.util.UUID;

public interface BookingClient {

    Optional<BookingDetailsDto> getBooking(UUID bookingId);

    boolean confirmBooking(UUID bookingId);
}
