package com.eventpulse.booking.dto;

public enum CheckInResult {
    /**
     * Ticket verified successfully, gate staff should allow entry.
     */
    ALLOW_ENTRY,

    /**
     * Ticket was already checked in (anti-passback / double-entry detected).
     */
    ALREADY_CHECKED_IN,

    /**
     * Cryptographic signature failed (forged or tampered QR code).
     */
    INVALID_SIGNATURE,

    /**
     * Ticket token not found in system database.
     */
    TICKET_NOT_FOUND,

    /**
     * Ticket is valid, but belongs to a different event.
     */
    WRONG_EVENT,

    /**
     * Ticket belongs to a cancelled or refunded booking.
     */
    TICKET_CANCELLED
}
