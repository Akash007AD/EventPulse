package com.eventpulse.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Injects OWASP recommended security response headers into every outbound HTTP response.
 * Prevents Clickjacking, MIME type sniffing, Cross-Site Scripting, and server fingerprinting.
 */
@Component
@Order(-100)
public class SecurityHeadersFilter implements WebFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        exchange.getResponse().beforeCommit(() -> {
            HttpHeaders headers = exchange.getResponse().getHeaders();
            headers.set("X-Content-Type-Options", "nosniff");
            headers.set("X-Frame-Options", "DENY");
            headers.set("X-XSS-Protection", "1; mode=block");
            headers.set("Referrer-Policy", "strict-origin-when-cross-origin");
            headers.set("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            headers.set("Content-Security-Policy", "default-src 'self'");
            headers.set("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
            headers.remove("Server");
            return Mono.empty();
        });
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
