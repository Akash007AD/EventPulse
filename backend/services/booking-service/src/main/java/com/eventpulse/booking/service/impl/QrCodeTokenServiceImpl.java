package com.eventpulse.booking.service.impl;

import com.eventpulse.booking.service.QrCodeTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class QrCodeTokenServiceImpl implements QrCodeTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String TOKEN_PREFIX = "EP";

    private final byte[] secretKeyBytes;

    public QrCodeTokenServiceImpl(@Value("${eventpulse.tickets.qr-hmac-secret:eventpulse-qr-secret-key-super-safe-32-chars}") String hmacSecret) {
        this.secretKeyBytes = hmacSecret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String generateToken(UUID ticketId, UUID bookingId, UUID eventId, Instant issuedAt) {
        long epochSecond = (issuedAt != null ? issuedAt : Instant.now()).getEpochSecond();
        String payload = String.format("%s:%s:%s:%d", ticketId, bookingId, eventId, epochSecond);
        String signature = calculateHmac(payload);

        // Format: EP.<ticketId>.<bookingId>.<eventId>.<epochSeconds>.<signature>
        return String.format("%s.%s.%s.%s.%d.%s", TOKEN_PREFIX, ticketId, bookingId, eventId, epochSecond, signature);
    }

    @Override
    public boolean verifyTokenSignature(String qrCodeToken) {
        if (qrCodeToken == null || qrCodeToken.isBlank()) {
            return false;
        }

        String[] parts = qrCodeToken.split("\\.");
        if (parts.length != 6 || !TOKEN_PREFIX.equals(parts[0])) {
            log.warn("Malformed QR token structure: expected 6 parts starting with {}", TOKEN_PREFIX);
            return false;
        }

        String ticketIdStr = parts[1];
        String bookingIdStr = parts[2];
        String eventIdStr = parts[3];
        String epochSecStr = parts[4];
        String receivedSignature = parts[5];

        String payload = String.format("%s:%s:%s:%s", ticketIdStr, bookingIdStr, eventIdStr, epochSecStr);
        String expectedSignature = calculateHmac(payload);

        // Constant-time comparison to prevent timing attacks
        return MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                receivedSignature.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    public Optional<UUID> extractTicketId(String qrCodeToken) {
        if (!verifyTokenSignature(qrCodeToken)) {
            return Optional.empty();
        }

        try {
            String[] parts = qrCodeToken.split("\\.");
            return Optional.of(UUID.fromString(parts[1]));
        } catch (Exception ex) {
            log.warn("Failed to extract ticket ID from token: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private String calculateHmac(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secretKeyBytes, HMAC_ALGORITHM));
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hmacBytes);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to calculate HMAC-SHA256 signature", ex);
        }
    }
}
