package com.eventpulse.booking.service;

import com.eventpulse.booking.dto.BookingResponse;
import com.eventpulse.booking.dto.CreateBookingRequest;
import com.eventpulse.booking.dto.PagedResponse;

import java.util.UUID;

public interface BookingService {

    BookingResponse createBooking(CreateBookingRequest request, String customerSubject);

    BookingResponse getBookingById(UUID id, String customerSubject);

    PagedResponse<BookingResponse> getMyBookings(String customerSubject, int page, int size);

    BookingResponse cancelBooking(UUID id, String customerSubject);

    BookingResponse confirmBooking(UUID id);
}
