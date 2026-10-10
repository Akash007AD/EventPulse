package com.eventpulse.booking.repository;

import com.eventpulse.booking.entity.Booking;
import com.eventpulse.booking.entity.BookingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @DisplayName("findByCustomerSubject should return only bookings for the given customer")
    void findByCustomerSubject_returnsCustomerBookings() {
        Booking booking1 = Booking.builder()
                .id(UUID.randomUUID())
                .customerSubject("alice")
                .eventId(UUID.randomUUID())
                .quantity(2)
                .amount(new BigDecimal("800.00"))
                .currency("INR")
                .status(BookingStatus.PENDING_PAYMENT)
                .expiresAt(Instant.now().plusSeconds(900))
                .build();

        Booking booking2 = Booking.builder()
                .id(UUID.randomUUID())
                .customerSubject("bob")
                .eventId(UUID.randomUUID())
                .quantity(1)
                .amount(new BigDecimal("400.00"))
                .currency("INR")
                .status(BookingStatus.PENDING_PAYMENT)
                .expiresAt(Instant.now().plusSeconds(900))
                .build();

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);

        Page<Booking> aliceBookings = bookingRepository.findByCustomerSubject("alice", PageRequest.of(0, 10));

        assertThat(aliceBookings.getContent()).hasSize(1);
        assertThat(aliceBookings.getContent().get(0).getCustomerSubject()).isEqualTo("alice");
        assertThat(aliceBookings.getContent().get(0).getAmount()).isEqualByComparingTo("800.00");
    }

    @Test
    @DisplayName("findByIdAndCustomerSubject should return empty if customer does not own booking")
    void findByIdAndCustomerSubject_enforcesOwnership() {
        UUID id = UUID.randomUUID();
        Booking booking = Booking.builder()
                .id(id)
                .customerSubject("alice")
                .eventId(UUID.randomUUID())
                .quantity(1)
                .amount(new BigDecimal("250.00"))
                .currency("INR")
                .status(BookingStatus.PENDING_PAYMENT)
                .expiresAt(Instant.now().plusSeconds(900))
                .build();

        bookingRepository.save(booking);

        Optional<Booking> unauthorizedLookup = bookingRepository.findByIdAndCustomerSubject(id, "eve");
        assertThat(unauthorizedLookup).isEmpty();

        Optional<Booking> authorizedLookup = bookingRepository.findByIdAndCustomerSubject(id, "alice");
        assertThat(authorizedLookup).isPresent();
    }
}
