package com.eventpulse.booking.service.impl;

import com.eventpulse.booking.client.EventCatalogClient;
import com.eventpulse.booking.dto.BookingResponse;
import com.eventpulse.booking.dto.CreateBookingRequest;
import com.eventpulse.booking.dto.EventSummaryDto;
import com.eventpulse.booking.dto.PagedResponse;
import com.eventpulse.booking.entity.Booking;
import com.eventpulse.booking.entity.BookingStatus;
import com.eventpulse.booking.exception.BookingNotFoundException;
import com.eventpulse.booking.exception.EventValidationException;
import com.eventpulse.booking.exception.InvalidBookingStateException;
import com.eventpulse.booking.repository.BookingRepository;
import com.eventpulse.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eventpulse.booking.exception.ConcurrentBookingException;
import com.eventpulse.booking.service.DistributedLockService;
import com.eventpulse.booking.service.TicketHoldService;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final EventCatalogClient eventCatalogClient;
    private final DistributedLockService distributedLockService;
    private final TicketHoldService ticketHoldService;
    private final com.eventpulse.booking.service.TicketService ticketService;

    @Value("${booking.hold-duration-minutes:15}")
    private int holdDurationMinutes;

    @Override
    public BookingResponse createBooking(CreateBookingRequest request, String customerSubject) {
        log.info("Initiating booking for event: {}, quantity: {}", request.getEventId(), request.getQuantity());

        String lockKey = "lock:event:" + request.getEventId();
        return distributedLockService.executeWithLock(lockKey, 3, 5, () -> {
            // 1. Verify Event existence and publication status via Event Service
            EventSummaryDto eventSummary = eventCatalogClient.getPublishedEvent(request.getEventId())
                    .orElseThrow(() -> new EventValidationException("Event not found or not published: " + request.getEventId()));

            if (!eventSummary.getCurrency().equalsIgnoreCase(request.getCurrency())) {
                throw new EventValidationException("Currency mismatch. Event uses: " + eventSummary.getCurrency() + ", but request specifies: " + request.getCurrency());
            }

            String effectiveCustomer = (customerSubject != null && !customerSubject.isBlank())
                    ? customerSubject.trim()
                    : (request.getCustomerSubject() != null && !request.getCustomerSubject().isBlank()
                        ? request.getCustomerSubject().trim()
                        : "customer-default");

            Instant now = Instant.now();
            Instant expiresAt = now.plus(holdDurationMinutes, ChronoUnit.MINUTES);

            Booking booking = Booking.builder()
                    .id(UUID.randomUUID())
                    .customerSubject(effectiveCustomer)
                    .eventId(request.getEventId())
                    .quantity(request.getQuantity())
                    .amount(request.getAmount())
                    .currency(request.getCurrency().trim().toUpperCase())
                    .status(BookingStatus.PENDING_PAYMENT)
                    .expiresAt(expiresAt)
                    .build();

            Booking saved = bookingRepository.save(booking);
            ticketHoldService.recordHold(saved.getId(), request.getEventId(), request.getQuantity(), Duration.ofMinutes(holdDurationMinutes));
            log.info("Booking created successfully with ID: {}, status: {}", saved.getId(), saved.getStatus());
            return BookingResponse.fromEntity(saved);
        });
    }

    @Override
    public BookingResponse getBookingById(UUID id, String customerSubject) {
        log.debug("Fetching booking: {}", id);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));

        // Ownership verification (Keycloak OAuth2 JWT subject)
        if (customerSubject != null && !customerSubject.isBlank() && !customerSubject.equals(booking.getCustomerSubject())) {
            log.warn("Customer {} attempted unauthorized access to booking {}", customerSubject, id);
            throw new org.springframework.security.access.AccessDeniedException("You do not have permission to access this booking.");
        }

        // Lazy hold expiry check
        if (booking.getStatus() == BookingStatus.PENDING_PAYMENT && Instant.now().isAfter(booking.getExpiresAt())) {
            log.info("Booking {} hold has expired. Updating status to EXPIRED.", id);
            booking.setStatus(BookingStatus.EXPIRED);
            booking = bookingRepository.save(booking);
            ticketHoldService.releaseHold(id);
        }

        return BookingResponse.fromEntity(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> getMyBookings(String customerSubject, int page, int size) {
        int boundedPage = Math.max(page, 0);
        int boundedSize = (size <= 0 || size > 100) ? 20 : size;

        Pageable pageable = PageRequest.of(boundedPage, boundedSize, Sort.by("createdAt").descending());
        Page<Booking> bookingPage = bookingRepository.findByCustomerSubject(customerSubject, pageable);

        return PagedResponse.of(bookingPage, BookingResponse::fromEntity);
    }

    @Override
    public BookingResponse cancelBooking(UUID id, String customerSubject) {
        log.info("Cancelling booking: {}", id);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));

        if (customerSubject != null && !customerSubject.isBlank() && !customerSubject.equals(booking.getCustomerSubject())) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have permission to cancel this booking.");
        }

        if (!booking.getStatus().canTransitionTo(BookingStatus.CANCELLED)) {
            throw new InvalidBookingStateException("Cannot cancel booking in status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking cancelled = bookingRepository.save(booking);
        ticketHoldService.releaseHold(id);
        ticketService.cancelTicketsForBooking(id);
        log.info("Booking {} and associated tickets cancelled successfully", id);
        return BookingResponse.fromEntity(cancelled);
    }

    @Override
    public BookingResponse confirmBooking(UUID id) {
        log.info("Confirming booking: {}", id);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));

        if (booking.getStatus() == BookingStatus.PENDING_PAYMENT && Instant.now().isAfter(booking.getExpiresAt())) {
            booking.setStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);
            ticketHoldService.releaseHold(id);
            throw new InvalidBookingStateException("Cannot confirm booking because the reservation hold has expired.");
        }

        if (!booking.getStatus().canTransitionTo(BookingStatus.CONFIRMED)) {
            throw new InvalidBookingStateException("Cannot confirm booking in status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        Booking confirmed = bookingRepository.save(booking);
        ticketHoldService.releaseHold(id);
        ticketService.issueTicketsForBooking(confirmed);
        log.info("Booking {} confirmed and tickets issued successfully", id);
        return BookingResponse.fromEntity(confirmed);
    }
}
