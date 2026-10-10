package com.eventpulse.booking.entity;

public enum TicketStatus {
    /**
     * Ticket has been issued upon confirmed payment; ready for gate admission.
     */
    ISSUED,

    /**
     * Ticket has been successfully scanned and attendee admitted to the event.
     */
    CHECKED_IN,

    /**
     * Ticket was invalidated due to booking cancellation or refund.
     */
    CANCELLED
}
