package com.eventpulse.booking.service;

import com.eventpulse.booking.service.impl.RedisTicketHoldService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RedisTicketHoldServiceTest {

    private RedisTicketHoldService holdService;

    @BeforeEach
    void setUp() {
        // Instantiate with null RedisTemplate to verify in-memory fallback holds
        holdService = new RedisTicketHoldService(null);
    }

    @Test
    @DisplayName("recordHold and isHoldActive: newly created hold is active")
    void recordHold_isActive() {
        UUID bookingId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        holdService.recordHold(bookingId, eventId, 2, Duration.ofMinutes(15));

        assertThat(holdService.isHoldActive(bookingId)).isTrue();
    }

    @Test
    @DisplayName("releaseHold: hold is no longer active once released")
    void releaseHold_becomesInactive() {
        UUID bookingId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        holdService.recordHold(bookingId, eventId, 2, Duration.ofMinutes(15));
        assertThat(holdService.isHoldActive(bookingId)).isTrue();

        holdService.releaseHold(bookingId);
        assertThat(holdService.isHoldActive(bookingId)).isFalse();
    }

    @Test
    @DisplayName("isHoldActive: expired hold returns false")
    void isHoldActive_expired_returnsFalse() {
        UUID bookingId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        // Expired hold (negative duration)
        holdService.recordHold(bookingId, eventId, 2, Duration.ofSeconds(-5));

        assertThat(holdService.isHoldActive(bookingId)).isFalse();
    }
}
