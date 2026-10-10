package com.eventpulse.booking.service.impl;

import com.eventpulse.booking.service.TicketHoldService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RedisTicketHoldService implements TicketHoldService {

    private final StringRedisTemplate redisTemplate;
    // In-memory fallback if Redis is unreachable or during test execution
    private final Map<UUID, Instant> inMemoryHolds = new ConcurrentHashMap<>();

    @Autowired(required = false)
    public RedisTicketHoldService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void recordHold(UUID bookingId, UUID eventId, int quantity, Duration duration) {
        String key = "booking:hold:" + bookingId;
        String payload = String.format("{\"bookingId\":\"%s\",\"eventId\":\"%s\",\"quantity\":%d,\"heldAt\":\"%s\"}",
                bookingId, eventId, quantity, Instant.now());

        log.info("Recording temporary ticket hold in Redis for booking '{}', event '{}', quantity: {}, TTL: {}s",
                bookingId, eventId, quantity, duration.toSeconds());

        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(key, payload, duration);
                return;
            } catch (Exception ex) {
                log.warn("Could not write hold to Redis (using in-memory fallback): {}", ex.getMessage());
            }
        }
        inMemoryHolds.put(bookingId, Instant.now().plus(duration));
    }

    @Override
    public void releaseHold(UUID bookingId) {
        String key = "booking:hold:" + bookingId;
        log.info("Releasing ticket hold for booking '{}'", bookingId);

        if (redisTemplate != null) {
            try {
                redisTemplate.delete(key);
            } catch (Exception ex) {
                log.warn("Could not delete hold from Redis: {}", ex.getMessage());
            }
        }
        inMemoryHolds.remove(bookingId);
    }

    @Override
    public boolean isHoldActive(UUID bookingId) {
        String key = "booking:hold:" + bookingId;

        if (redisTemplate != null) {
            try {
                Boolean hasKey = redisTemplate.hasKey(key);
                return Boolean.TRUE.equals(hasKey);
            } catch (Exception ex) {
                log.debug("Redis read check fallback: {}", ex.getMessage());
            }
        }

        Instant expiry = inMemoryHolds.get(bookingId);
        if (expiry == null) {
            return false;
        }
        if (Instant.now().isAfter(expiry)) {
            inMemoryHolds.remove(bookingId);
            return false;
        }
        return true;
    }
}
