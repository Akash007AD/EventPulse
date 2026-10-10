package com.eventpulse.booking.service;

import com.eventpulse.booking.dto.CheckInResponse;
import com.eventpulse.booking.dto.CheckInResult;
import com.eventpulse.booking.dto.ScanQrRequest;
import com.eventpulse.booking.dto.TicketResponse;
import com.eventpulse.booking.entity.Booking;
import com.eventpulse.booking.entity.BookingStatus;
import com.eventpulse.booking.entity.Ticket;
import com.eventpulse.booking.entity.TicketStatus;
import com.eventpulse.booking.repository.TicketRepository;
import com.eventpulse.booking.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private QrCodeTokenService qrCodeTokenService;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Test
    @DisplayName("issueTicketsForBooking creates signed tickets according to booking quantity")
    void issueTicketsForBooking_success() {
        UUID bookingId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Booking booking = Booking.builder()
                .id(bookingId)
                .customerSubject("cust-bob")
                .eventId(eventId)
                .quantity(2)
                .amount(new BigDecimal("100.00"))
                .currency("INR")
                .status(BookingStatus.CONFIRMED)
                .build();

        when(qrCodeTokenService.generateToken(any(), eq(bookingId), eq(eventId), any()))
                .thenReturn("EP.mocked.token.123");
        when(ticketRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<TicketResponse> tickets = ticketService.issueTicketsForBooking(booking);

        assertThat(tickets).hasSize(2);
        assertThat(tickets.get(0).getCustomerSubject()).isEqualTo("cust-bob");
        assertThat(tickets.get(0).getStatus()).isEqualTo(TicketStatus.ISSUED);
        assertThat(tickets.get(0).getQrCodeToken()).isEqualTo("EP.mocked.token.123");
    }

    @Test
    @DisplayName("scanAndCheckIn succeeds and marks ticket as CHECKED_IN")
    void scanAndCheckIn_success() {
        UUID ticketId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        String validToken = "EP.valid.token";

        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .bookingId(bookingId)
                .eventId(eventId)
                .customerSubject("cust-bob")
                .ticketNumber("TKT-001")
                .qrCodeToken(validToken)
                .status(TicketStatus.ISSUED)
                .build();

        ScanQrRequest request = ScanQrRequest.builder()
                .qrCodeToken(validToken)
                .eventId(eventId)
                .gateLocation("North Entrance Gate 3")
                .build();

        when(qrCodeTokenService.verifyTokenSignature(validToken)).thenReturn(true);
        when(ticketRepository.findByQrCodeToken(validToken)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        CheckInResponse response = ticketService.scanAndCheckIn(request, "staff-john");

        assertThat(response.getResult()).isEqualTo(CheckInResult.ALLOW_ENTRY);
        assertThat(response.isValid()).isTrue();
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.CHECKED_IN);
        assertThat(ticket.getCheckedInBy()).isEqualTo("staff-john");
        assertThat(ticket.getCheckedInAt()).isNotNull();
    }

    @Test
    @DisplayName("scanAndCheckIn rejects double entry (anti-passback protection)")
    void scanAndCheckIn_alreadyCheckedIn_antiPassback() {
        UUID ticketId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        String validToken = "EP.valid.token";
        Instant pastCheckIn = Instant.now().minusSeconds(120);

        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .bookingId(bookingId)
                .eventId(eventId)
                .customerSubject("cust-bob")
                .ticketNumber("TKT-001")
                .qrCodeToken(validToken)
                .status(TicketStatus.CHECKED_IN)
                .checkedInAt(pastCheckIn)
                .checkedInBy("staff-alice")
                .build();

        ScanQrRequest request = ScanQrRequest.builder()
                .qrCodeToken(validToken)
                .eventId(eventId)
                .gateLocation("South Gate 1")
                .build();

        when(qrCodeTokenService.verifyTokenSignature(validToken)).thenReturn(true);
        when(ticketRepository.findByQrCodeToken(validToken)).thenReturn(Optional.of(ticket));

        CheckInResponse response = ticketService.scanAndCheckIn(request, "staff-john");

        assertThat(response.getResult()).isEqualTo(CheckInResult.ALREADY_CHECKED_IN);
        assertThat(response.isValid()).isFalse();
        assertThat(response.getMessage()).contains("ENTRY DENIED");
        verify(ticketRepository, never()).save(ticket);
    }

    @Test
    @DisplayName("scanAndCheckIn rejects ticket scanned at the wrong event")
    void scanAndCheckIn_wrongEvent() {
        UUID ticketId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        UUID correctEventId = UUID.randomUUID();
        UUID wrongEventId = UUID.randomUUID();
        String token = "EP.token";

        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .bookingId(bookingId)
                .eventId(correctEventId)
                .customerSubject("cust-bob")
                .ticketNumber("TKT-001")
                .qrCodeToken(token)
                .status(TicketStatus.ISSUED)
                .build();

        ScanQrRequest request = ScanQrRequest.builder()
                .qrCodeToken(token)
                .eventId(wrongEventId)
                .build();

        when(qrCodeTokenService.verifyTokenSignature(token)).thenReturn(true);
        when(ticketRepository.findByQrCodeToken(token)).thenReturn(Optional.of(ticket));

        CheckInResponse response = ticketService.scanAndCheckIn(request, "staff-gate");

        assertThat(response.getResult()).isEqualTo(CheckInResult.WRONG_EVENT);
        assertThat(response.isValid()).isFalse();
    }

    @Test
    @DisplayName("scanAndCheckIn rejects forged or tampered token signature")
    void scanAndCheckIn_invalidCryptographicSignature() {
        String forgedToken = "EP.forged.token";

        ScanQrRequest request = ScanQrRequest.builder()
                .qrCodeToken(forgedToken)
                .eventId(UUID.randomUUID())
                .build();

        when(qrCodeTokenService.verifyTokenSignature(forgedToken)).thenReturn(false);

        CheckInResponse response = ticketService.scanAndCheckIn(request, "staff-gate");

        assertThat(response.getResult()).isEqualTo(CheckInResult.INVALID_SIGNATURE);
        assertThat(response.isValid()).isFalse();
        verify(ticketRepository, never()).findByQrCodeToken(any());
    }

    @Test
    @DisplayName("scanAndCheckIn rejects cancelled booking ticket")
    void scanAndCheckIn_cancelledTicket() {
        UUID ticketId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        String token = "EP.token";

        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .bookingId(bookingId)
                .eventId(eventId)
                .customerSubject("cust-bob")
                .ticketNumber("TKT-001")
                .qrCodeToken(token)
                .status(TicketStatus.CANCELLED)
                .build();

        ScanQrRequest request = ScanQrRequest.builder()
                .qrCodeToken(token)
                .eventId(eventId)
                .build();

        when(qrCodeTokenService.verifyTokenSignature(token)).thenReturn(true);
        when(ticketRepository.findByQrCodeToken(token)).thenReturn(Optional.of(ticket));

        CheckInResponse response = ticketService.scanAndCheckIn(request, "staff-gate");

        assertThat(response.getResult()).isEqualTo(CheckInResult.TICKET_CANCELLED);
        assertThat(response.isValid()).isFalse();
    }

    @Test
    @DisplayName("cancelTicketsForBooking marks all issued tickets as CANCELLED")
    void cancelTicketsForBooking_marksAllCancelled() {
        UUID bookingId = UUID.randomUUID();
        Ticket t1 = Ticket.builder().id(UUID.randomUUID()).bookingId(bookingId).status(TicketStatus.ISSUED).build();
        Ticket t2 = Ticket.builder().id(UUID.randomUUID()).bookingId(bookingId).status(TicketStatus.ISSUED).build();

        when(ticketRepository.findByBookingId(bookingId)).thenReturn(List.of(t1, t2));
        when(ticketRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        ticketService.cancelTicketsForBooking(bookingId);

        assertThat(t1.getStatus()).isEqualTo(TicketStatus.CANCELLED);
        assertThat(t2.getStatus()).isEqualTo(TicketStatus.CANCELLED);
        verify(ticketRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("getTicketById throws AccessDeniedException when customer subject does not match")
    void getTicketById_forbidden_throwsAccessDenied() {
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .customerSubject("owner-cust")
                .build();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.getTicketById(ticketId, "other-cust"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("getTicketById succeeds when customer subject matches")
    void getTicketById_success() {
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .customerSubject("owner-cust")
                .ticketNumber("TKT-001")
                .status(TicketStatus.ISSUED)
                .build();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        TicketResponse res = ticketService.getTicketById(ticketId, "owner-cust");
        assertThat(res).isNotNull();
        assertThat(res.getTicketNumber()).isEqualTo("TKT-001");
    }
}
