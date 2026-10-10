package com.eventpulse.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance edge rate limiter implementing the Token Bucket algorithm.
 * Applies stricter rate limits to booking and payment endpoints to mitigate
 * ticket-scalping bots, brute-force attacks, and DDoS attempts.
 */
@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    @Value("${eventpulse.ratelimit.enabled:true}")
    private boolean enabled;

    // In-memory token bucket tracking per key (IP:route-tier)
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    private static class TokenBucket {
        final int capacity;
        final double replenishRatePerSecond;
        double tokens;
        long lastRefillTimestampNanos;

        TokenBucket(int capacity, double replenishRatePerSecond) {
            this.capacity = capacity;
            this.replenishRatePerSecond = replenishRatePerSecond;
            this.tokens = capacity;
            this.lastRefillTimestampNanos = System.nanoTime();
        }

        synchronized boolean tryConsume() {
            refill();
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.nanoTime();
            double elapsedSeconds = (now - lastRefillTimestampNanos) / 1_000_000_000.0;
            if (elapsedSeconds > 0) {
                tokens = Math.min(capacity, tokens + (elapsedSeconds * replenishRatePerSecond));
                lastRefillTimestampNanos = now;
            }
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!enabled) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();

        // Skip actuator health / metrics and fallback from rate limiting
        if (path.startsWith("/actuator") || path.startsWith("/fallback")) {
            return chain.filter(exchange);
        }

        String clientIp = resolveClientIp(exchange);
        Tier tier = resolveTier(path);
        String bucketKey = clientIp + ":" + tier.name();

        TokenBucket bucket = buckets.computeIfAbsent(bucketKey, k -> new TokenBucket(tier.capacity, tier.replenishRatePerSecond));

        if (!bucket.tryConsume()) {
            log.warn("Rate limit exceeded for client IP [{}] on path [{}] (tier: {})", clientIp, path, tier);
            return rejectTooManyRequests(exchange);
        }

        // Clean up stale buckets if cache exceeds 10,000 entries
        if (buckets.size() > 10_000) {
            buckets.clear();
        }

        return chain.filter(exchange);
    }

    private String resolveClientIp(ServerWebExchange exchange) {
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return "127.0.0.1";
    }

    private Tier resolveTier(String path) {
        if (path.startsWith("/api/v1/payments")) {
            return Tier.PAYMENT; // 10 burst, ~10 per minute
        } else if (path.startsWith("/api/v1/bookings")) {
            return Tier.BOOKING; // 20 burst, ~20 per minute
        } else {
            return Tier.GENERAL; // 60 burst, ~60 per minute
        }
    }

    private Mono<Void> rejectTooManyRequests(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        response.getHeaders().set(HttpHeaders.RETRY_AFTER, "60");

        String jsonBody = "{\"type\":\"https://api.eventpulse.com/errors/too-many-requests\",\"title\":\"Too Many Requests\",\"status\":429,\"detail\":\"Rate limit exceeded. Please try again after 60 seconds.\"}";

        byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
        response.getHeaders().setContentLength(bytes.length);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }

    private enum Tier {
        PAYMENT(10, 10.0 / 60.0),
        BOOKING(20, 20.0 / 60.0),
        GENERAL(60, 60.0 / 60.0);

        final int capacity;
        final double replenishRatePerSecond;

        Tier(int capacity, double replenishRatePerSecond) {
            this.capacity = capacity;
            this.replenishRatePerSecond = replenishRatePerSecond;
        }
    }
}
