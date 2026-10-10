package com.eventpulse.booking.service;

import com.eventpulse.booking.client.EventCatalogClient;
import com.eventpulse.booking.dto.BookingResponse;
import com.eventpulse.booking.dto.CreateBookingRequest;
import com.eventpulse.booking.dto.EventSummaryDto;
import com.eventpulse.booking.entity.Booking;
import com.eventpulse.booking.entity.BookingStatus;
import com.eventpulse.booking.exception.ConcurrentBookingException;
import com.eventpulse.booking.exception.EventValidationException;
import com.eventpulse.booking.exception.InvalidBookingStateException;
import com.eventpulse.booking.repository.BookingRepository;
import com.eventpulse.booking.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EventCatalogClient eventCatalogClient;

    @Mock
    private DistributedLockService distributedLockService;

    @Mock
    private TicketHoldService ticketHoldService;

    @Mock
    private TicketService ticketService;

    private BookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(bookingRepository, eventCatalogClient, distributedLockService, ticketHoldService, ticketService);
        ReflectionTestUtils.setField(bookingService, "holdDurationMinutes", 15);

        lenient().when(distributedLockService.executeWithLock(anyString(), anyLong(), anyLong(), any()))
                .thenAnswer(inv -> {
                    Supplier<?> supplier = inv.getArgument(3);
                    return supplier != null ? supplier.get() : null;
                });
    }

    @Test
    @DisplayName("createBooking should succeed and set status to PENDING_PAYMENT when event is valid")
    void createBooking_success() {
        UUID eventId = UUID.randomUUID();
        CreateBookingRequest request = new CreateBookingRequest(
                eventId, 2, new BigDecimal("998.00"), "INR", "cust-1"
        );

        EventSummaryDto eventSummary = new EventSummaryDto(
                eventId, "Spring AI Summit", "PUBLISHED", "INR"
        );

        when(eventCatalogClient.getPublishedEvent(eventId)).thenReturn(Optional.of(eventSummary));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.createBooking(request, "cust-1");

        assertThat(response).isNotNull();
        assertThat(response.getEventId()).isEqualTo(eventId);
        assertThat(response.getQuantity()).isEqualTo(2);
        assertThat(response.getAmount()).isEqualByComparingTo("998.00");
        assertThat(response.getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
        assertThat(captor.getValue().getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    @DisplayName("createBooking should fail when event does not exist in Event Service")
    void createBooking_failsWhenEventNotFound() {
        UUID eventId = UUID.randomUUID();
        CreateBookingRequest request = new CreateBookingRequest(
                eventId, 2, new BigDecimal("998.00"), "INR", "cust-1"
        );

        when(eventCatalogClient.getPublishedEvent(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(request, "cust-1"))
                .isInstanceOf(EventValidationException.class)
                .hasMessageContaining("Event not found or not published");
    }

    @Test
    @DisplayName("createBooking should fail when currency does not match event currency")
    void createBooking_failsWhenCurrencyMismatch() {
        UUID eventId = UUID.randomUUID();
        CreateBookingRequest request = new CreateBookingRequest(
                eventId, 1, new BigDecimal("100.00"), "USD", "cust-1"
        );

        EventSummaryDto eventSummary = new EventSummaryDto(
                eventId, "India Tech Fest", "PUBLISHED", "INR"
        );

        when(eventCatalogClient.getPublishedEvent(eventId)).thenReturn(Optional.of(eventSummary));

        assertThatThrownBy(() -> bookingService.createBooking(request, "cust-1"))
                .isInstanceOf(EventValidationException.class)
                .hasMessageContaining("Currency mismatch");
    }

    @Test
    @DisplayName("getBookingById should lazily transition to EXPIRED if hold window has passed")
    void getBookingById_expiresHoldLazily() {
        UUID bookingId = UUID.randomUUID();
        Booking expiredBooking = Booking.builder()
                .id(bookingId)
                .customerSubject("cust-1")
                .eventId(UUID.randomUUID())
                .quantity(1)
                .amount(new BigDecimal("500.00"))
                .currency("INR")
                .status(BookingStatus.PENDING_PAYMENT)
                .expiresAt(Instant.now().minus(5, ChronoUnit.MINUTES)) // Past
                .build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(expiredBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.getBookingById(bookingId, "cust-1");

        assertThat(response.getStatus()).isEqualTo(BookingStatus.EXPIRED);
        assertThat(expiredBooking.getStatus()).isEqualTo(BookingStatus.EXPIRED);
    }

    @Test
    @DisplayName("cancelBooking should succeed for PENDING_PAYMENT booking")
    void cancelBooking_success() {
        UUID bookingId = UUID.randomUUID();
        Booking pending = Booking.builder()
                .id(bookingId)
                .customerSubject("cust-1")
                .eventId(UUID.randomUUID())
                .quantity(1)
                .amount(new BigDecimal("500.00"))
                .currency("INR")
                .status(BookingStatus.PENDING_PAYMENT)
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(pending));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.cancelBooking(bookingId, "cust-1");

        assertThat(response.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(ticketService).cancelTicketsForBooking(bookingId);
    }

    @Test
    @DisplayName("confirmBooking should transition PENDING_PAYMENT to CONFIRMED")
    void confirmBooking_success() {
        UUID bookingId = UUID.randomUUID();
        Booking pending = Booking.builder()
                .id(bookingId)
                .customerSubject("cust-1")
                .eventId(UUID.randomUUID())
                .quantity(2)
                .amount(new BigDecimal("1000.00"))
                .currency("INR")
                .status(BookingStatus.PENDING_PAYMENT)
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(pending));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.confirmBooking(bookingId);

        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(ticketService).issueTicketsForBooking(any(Booking.class));
    }

    @Test
    @DisplayName("confirmBooking should fail if reservation hold has expired")
    void confirmBooking_failsWhenExpired() {
        UUID bookingId = UUID.randomUUID();
        Booking expired = Booking.builder()
                .id(bookingId)
                .customerSubject("cust-1")
                .eventId(UUID.randomUUID())
                .quantity(2)
                .amount(new BigDecimal("1000.00"))
                .currency("INR")
                .status(BookingStatus.PENDING_PAYMENT)
                .expiresAt(Instant.now().minus(2, ChronoUnit.MINUTES))
                .build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> bookingService.confirmBooking(bookingId))
                .isInstanceOf(InvalidBookingStateException.class)
                .hasMessageContaining("reservation hold has expired");
    }

    @Test
    @DisplayName("createBooking when lock cannot be acquired throws ConcurrentBookingException")
    void createBooking_lockAcquisitionFails_throwsConcurrentException() {
        UUID eventId = UUID.randomUUID();
        CreateBookingRequest request = new CreateBookingRequest(
                eventId, 2, new BigDecimal("998.00"), "INR", "cust-1"
        );

        doThrow(new ConcurrentBookingException("High demand detected for event " + eventId + ". Please retry your reservation."))
                .when(distributedLockService).executeWithLock(anyString(), anyLong(), anyLong(), any());

        assertThatThrownBy(() -> bookingService.createBooking(request, "cust-1"))
                .isInstanceOf(ConcurrentBookingException.class)
                .hasMessageContaining("High demand detected");
    }
}
