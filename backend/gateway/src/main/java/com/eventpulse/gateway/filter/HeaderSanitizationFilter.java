package com.eventpulse.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * Strips untrusted client-supplied headers to prevent identity and role spoofing attacks.
 * External clients must not be permitted to inject X-User-Id, X-User-Roles, or internal tokens.
 */
@Component
@Order(-80)
public class HeaderSanitizationFilter implements WebFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(HeaderSanitizationFilter.class);

    private static final List<String> SENSITIVE_HEADERS_TO_STRIP = Arrays.asList(
            "X-User-Id",
            "X-User-Subject",
            "X-User-Roles",
            "X-User-Email",
            "X-Internal-Service",
            "X-Admin-Bypass",
            "X-EventPulse-Internal",
            "X-Original-User"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        boolean hasSpoofedHeaders = SENSITIVE_HEADERS_TO_STRIP.stream()
                .anyMatch(h -> request.getHeaders().containsKey(h));

        if (hasSpoofedHeaders) {
            log.warn("Detected attempt to inject sensitive internal headers from remote {}: stripping headers",
                    request.getRemoteAddress());
        }

        ServerHttpRequest mutatedRequest = request.mutate()
                .headers(httpHeaders -> {
                    for (String header : SENSITIVE_HEADERS_TO_STRIP) {
                        httpHeaders.remove(header);
                    }
                })
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -80;
    }
}
