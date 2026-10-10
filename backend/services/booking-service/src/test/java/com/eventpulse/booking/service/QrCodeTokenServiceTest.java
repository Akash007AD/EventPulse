package com.eventpulse.booking.service;

import com.eventpulse.booking.service.impl.QrCodeTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class QrCodeTokenServiceTest {

    private QrCodeTokenServiceImpl qrCodeTokenService;
    private final String secretKey = "test-very-secure-hmac-sha256-signing-secret-key-123456";

    @BeforeEach
    void setUp() {
        qrCodeTokenService = new QrCodeTokenServiceImpl(secretKey);
    }

    @Test
    @DisplayName("generateToken produces formatted token with valid HMAC signature and can be verified")
    void generateToken_producesValidToken() {
        UUID ticketId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Instant now = Instant.now();

        String token = qrCodeTokenService.generateToken(ticketId, bookingId, eventId, now);

        assertThat(token).isNotNull();
        assertThat(token).startsWith("EP.");
        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(6);
        assertThat(parts[0]).isEqualTo("EP");
        assertThat(parts[1]).isEqualTo(ticketId.toString());
        assertThat(parts[2]).isEqualTo(bookingId.toString());
        assertThat(parts[3]).isEqualTo(eventId.toString());
        assertThat(Long.parseLong(parts[4])).isEqualTo(now.getEpochSecond());
        assertThat(parts[5]).matches("[0-9a-f]{64}");

        // Signature should verify successfully
        boolean valid = qrCodeTokenService.verifyTokenSignature(token);
        assertThat(valid).isTrue();

        // Extracted ticket ID matches
        Optional<UUID> extractedTicketId = qrCodeTokenService.extractTicketId(token);
        assertThat(extractedTicketId).isPresent().contains(ticketId);
    }

    @Test
    @DisplayName("verifyTokenSignature fails when signature is tampered or payload altered")
    void verifyTokenSignature_tamperedToken_returnsFalse() {
        UUID ticketId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        String token = qrCodeTokenService.generateToken(ticketId, bookingId, eventId, Instant.now());

        // Corrupt signature
        String tamperedToken = token.substring(0, token.length() - 4) + "0000";
        assertThat(qrCodeTokenService.verifyTokenSignature(tamperedToken)).isFalse();
        assertThat(qrCodeTokenService.extractTicketId(tamperedToken)).isEmpty();

        // Alter ticket ID part in token
        String[] parts = token.split("\\.");
        parts[1] = UUID.randomUUID().toString();
        String alteredToken = String.join(".", parts);
        assertThat(qrCodeTokenService.verifyTokenSignature(alteredToken)).isFalse();
    }

    @Test
    @DisplayName("verifyTokenSignature fails on malformed token strings or null inputs")
    void verifyTokenSignature_malformedInputs_returnsFalse() {
        assertThat(qrCodeTokenService.verifyTokenSignature(null)).isFalse();
        assertThat(qrCodeTokenService.verifyTokenSignature("")).isFalse();
        assertThat(qrCodeTokenService.verifyTokenSignature("invalid.token")).isFalse();
        assertThat(qrCodeTokenService.verifyTokenSignature("EP.abc.def")).isFalse();
        assertThat(qrCodeTokenService.extractTicketId("not-a-token")).isEmpty();
    }
}
