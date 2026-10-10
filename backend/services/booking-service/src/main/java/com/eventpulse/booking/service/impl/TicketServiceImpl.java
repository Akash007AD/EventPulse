package com.eventpulse.booking.service.impl;

import com.eventpulse.booking.dto.CheckInResponse;
import com.eventpulse.booking.dto.CheckInResult;
import com.eventpulse.booking.dto.ScanQrRequest;
import com.eventpulse.booking.dto.TicketResponse;
import com.eventpulse.booking.entity.Booking;
import com.eventpulse.booking.entity.Ticket;
import com.eventpulse.booking.entity.TicketStatus;
import com.eventpulse.booking.repository.TicketRepository;
import com.eventpulse.booking.service.QrCodeTokenService;
import com.eventpulse.booking.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final QrCodeTokenService qrCodeTokenService;

    @Override
    @Transactional
    public List<TicketResponse> issueTicketsForBooking(Booking booking) {
        log.info("Issuing tickets for booking ID: {}", booking.getId());

        // Idempotency check: if tickets were already issued for this booking, return existing
        List<Ticket> existing = ticketRepository.findByBookingId(booking.getId());
        if (!existing.isEmpty()) {
            log.info("Found {} existing tickets for booking {}", existing.size(), booking.getId());
            return existing.stream().map(TicketResponse::fromEntity).toList();
        }

        String shortBookingId = booking.getId().toString().substring(0, 8).toUpperCase();
        Instant now = Instant.now();
        List<Ticket> toSave = new ArrayList<>();

        for (int i = 1; i <= booking.getQuantity(); i++) {
            UUID ticketId = UUID.randomUUID();
            String ticketNumber = String.format("TKT-%s-%02d", shortBookingId, i);
            String qrToken = qrCodeTokenService.generateToken(ticketId, booking.getId(), booking.getEventId(), now);

            Ticket ticket = Ticket.builder()
                    .id(ticketId)
                    .bookingId(booking.getId())
                    .eventId(booking.getEventId())
                    .customerSubject(booking.getCustomerSubject())
                    .ticketNumber(ticketNumber)
                    .qrCodeToken(qrToken)
                    .status(TicketStatus.ISSUED)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            toSave.add(ticket);
        }

        List<Ticket> savedTickets = ticketRepository.saveAll(toSave);
        log.info("Successfully issued and saved {} tickets for booking {}", savedTickets.size(), booking.getId());
        return savedTickets.stream().map(TicketResponse::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsForBooking(UUID bookingId, String customerSubject) {
        List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);
        if (tickets.isEmpty()) {
            return List.of();
        }

        if (customerSubject != null && !customerSubject.isBlank()
                && !customerSubject.equals(tickets.get(0).getCustomerSubject())) {
            throw new AccessDeniedException("You do not have permission to view tickets for this booking.");
        }

        return tickets.stream().map(TicketResponse::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(UUID ticketId, String customerSubject) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NoSuchElementException("Ticket not found with ID: " + ticketId));

        if (customerSubject != null && !customerSubject.isBlank()
                && !customerSubject.equals(ticket.getCustomerSubject())) {
            throw new AccessDeniedException("You do not have permission to view this ticket.");
        }

        return TicketResponse.fromEntity(ticket);
    }

    @Override
    @Transactional
    public void cancelTicketsForBooking(UUID bookingId) {
        log.info("Cancelling all tickets for booking ID: {}", bookingId);
        List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);
        for (Ticket ticket : tickets) {
            ticket.setStatus(TicketStatus.CANCELLED);
        }
        ticketRepository.saveAll(tickets);
    }

    @Override
    @Transactional
    public CheckInResponse scanAndCheckIn(ScanQrRequest request, String staffSubject) {
        Instant scanTime = Instant.now();
        String qrToken = request.getQrCodeToken().trim();

        // Step 1: Cryptographic HMAC signature check (anti-forgery)
        if (!qrCodeTokenService.verifyTokenSignature(qrToken)) {
            log.warn("Ticket scan rejected: cryptographic signature verification failed for token");
            return CheckInResponse.builder()
                    .valid(false)
                    .result(CheckInResult.INVALID_SIGNATURE)
                    .message("ENTRY DENIED: Forged or tampered ticket QR code detected.")
                    .scanTimestamp(scanTime)
                    .build();
        }

        // Step 2: Database ticket lookup
        Ticket ticket = ticketRepository.findByQrCodeToken(qrToken).orElse(null);
        if (ticket == null) {
            log.warn("Ticket scan rejected: valid signature but ticket record not found in database");
            return CheckInResponse.builder()
                    .valid(false)
                    .result(CheckInResult.TICKET_NOT_FOUND)
                    .message("ENTRY DENIED: Ticket record not found in platform database.")
                    .scanTimestamp(scanTime)
                    .build();
        }

        // Step 3: Event match verification
        if (request.getEventId() != null && !request.getEventId().equals(ticket.getEventId())) {
            log.warn("Ticket scan rejected: ticket {} is for event {} but scanned at event {}",
                    ticket.getTicketNumber(), ticket.getEventId(), request.getEventId());
            return CheckInResponse.builder()
                    .valid(false)
                    .result(CheckInResult.WRONG_EVENT)
                    .message("ENTRY DENIED: This ticket is for a different event.")
                    .scanTimestamp(scanTime)
                    .ticket(TicketResponse.fromEntity(ticket))
                    .build();
        }

        // Step 4: Status validation (cancelled / anti-passback double entry check)
        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            log.warn("Ticket scan rejected: ticket {} is cancelled", ticket.getTicketNumber());
            return CheckInResponse.builder()
                    .valid(false)
                    .result(CheckInResult.TICKET_CANCELLED)
                    .message("ENTRY DENIED: This ticket was cancelled or refunded.")
                    .scanTimestamp(scanTime)
                    .ticket(TicketResponse.fromEntity(ticket))
                    .build();
        }

        if (ticket.getStatus() == TicketStatus.CHECKED_IN) {
            log.warn("Ticket scan rejected: ticket {} was ALREADY checked in at {} by {}",
                    ticket.getTicketNumber(), ticket.getCheckedInAt(), ticket.getCheckedInBy());
            return CheckInResponse.builder()
                    .valid(false)
                    .result(CheckInResult.ALREADY_CHECKED_IN)
                    .message(String.format("ENTRY DENIED: Ticket was already scanned and used at %s by gate staff '%s'.",
                            ticket.getCheckedInAt(), ticket.getCheckedInBy()))
                    .scanTimestamp(scanTime)
                    .ticket(TicketResponse.fromEntity(ticket))
                    .build();
        }

        // Step 5: Successful check-in admission
        String gateStaff = (staffSubject != null && !staffSubject.isBlank()) ? staffSubject : "GATE-STAFF";
        ticket.setStatus(TicketStatus.CHECKED_IN);
        ticket.setCheckedInAt(scanTime);
        ticket.setCheckedInBy(gateStaff);

        Ticket saved = ticketRepository.save(ticket);
        log.info("TICKET ADMITTED: Ticket {} (ID: {}) checked in successfully by {}",
                saved.getTicketNumber(), saved.getId(), gateStaff);

        return CheckInResponse.builder()
                .valid(true)
                .result(CheckInResult.ALLOW_ENTRY)
                .message(String.format("ENTRY GRANTED: Valid ticket %s verified. Welcome to the event!", saved.getTicketNumber()))
                .scanTimestamp(scanTime)
                .ticket(TicketResponse.fromEntity(saved))
                .build();
    }
}
