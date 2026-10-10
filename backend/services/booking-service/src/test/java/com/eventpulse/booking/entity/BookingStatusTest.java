package com.eventpulse.booking.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BookingStatusTest {

    @Test
    @DisplayName("PENDING_PAYMENT can transition to CONFIRMED, CANCELLED, or EXPIRED")
    void pendingPayment_validTransitions() {
        assertThat(BookingStatus.PENDING_PAYMENT.canTransitionTo(BookingStatus.CONFIRMED)).isTrue();
        assertThat(BookingStatus.PENDING_PAYMENT.canTransitionTo(BookingStatus.CANCELLED)).isTrue();
        assertThat(BookingStatus.PENDING_PAYMENT.canTransitionTo(BookingStatus.EXPIRED)).isTrue();
        assertThat(BookingStatus.PENDING_PAYMENT.canTransitionTo(BookingStatus.REFUNDED)).isFalse();
    }

    @Test
    @DisplayName("CONFIRMED can transition to REFUND_PENDING or CANCELLED, but not back to PENDING_PAYMENT")
    void confirmed_validTransitions() {
        assertThat(BookingStatus.CONFIRMED.canTransitionTo(BookingStatus.REFUND_PENDING)).isTrue();
        assertThat(BookingStatus.CONFIRMED.canTransitionTo(BookingStatus.CANCELLED)).isTrue();
        assertThat(BookingStatus.CONFIRMED.canTransitionTo(BookingStatus.PENDING_PAYMENT)).isFalse();
    }

    @Test
    @DisplayName("Terminal states (CANCELLED, EXPIRED, REFUNDED) cannot transition to any state")
    void terminalStates_noTransitionsAllowed() {
        for (BookingStatus target : BookingStatus.values()) {
            assertThat(BookingStatus.CANCELLED.canTransitionTo(target)).isFalse();
            assertThat(BookingStatus.EXPIRED.canTransitionTo(target)).isFalse();
            assertThat(BookingStatus.REFUNDED.canTransitionTo(target)).isFalse();
        }
    }
}
