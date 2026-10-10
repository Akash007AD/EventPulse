package com.eventpulse.booking.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface QrCodeTokenService {

    /**
     * Generates a tamper-proof, cryptographically signed QR code token.
     */
    String generateToken(UUID ticketId, UUID bookingId, UUID eventId, Instant issuedAt);

    /**
     * Verifies the cryptographic HMAC signature of a scanned QR code token.
     */
    boolean verifyTokenSignature(String qrCodeToken);

    /**
     * Parses the ticket identity from a verified QR code token.
     */
    Optional<UUID> extractTicketId(String qrCodeToken);
}
