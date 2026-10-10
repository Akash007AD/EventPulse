package com.eventpulse.booking.entity;

public enum BookingStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    CANCELLED,
    EXPIRED,
    REFUND_PENDING,
    REFUNDED;

    public boolean canTransitionTo(BookingStatus newStatus) {
        return switch (this) {
            case PENDING_PAYMENT -> newStatus == CONFIRMED || newStatus == CANCELLED || newStatus == EXPIRED;
            case CONFIRMED -> newStatus == REFUND_PENDING || newStatus == CANCELLED;
            case REFUND_PENDING -> newStatus == REFUNDED;
            case CANCELLED, EXPIRED, REFUNDED -> false;
        };
    }
}
