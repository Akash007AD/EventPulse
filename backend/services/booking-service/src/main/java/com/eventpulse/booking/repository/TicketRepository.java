package com.eventpulse.booking.repository;

import com.eventpulse.booking.entity.Ticket;
import com.eventpulse.booking.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    List<Ticket> findByBookingId(UUID bookingId);

    Optional<Ticket> findByQrCodeToken(String qrCodeToken);

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    List<Ticket> findByCustomerSubject(String customerSubject);

    long countByEventIdAndStatus(UUID eventId, TicketStatus status);
}
