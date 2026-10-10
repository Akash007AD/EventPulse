package com.eventpulse.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Perimeter defense WebFilter blocking path traversal, null-byte injections, script tags,
 * and malicious query parameters at the outermost HTTP layer before routing.
 */
@Component
@Order(-90)
public class MaliciousRequestGuardFilter implements WebFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(MaliciousRequestGuardFilter.class);

    private static final int MAX_URI_LENGTH = 2048;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        URI uri = exchange.getRequest().getURI();
        String fullUriString = uri.toString();
        String lowerRawUri = fullUriString.toLowerCase(Locale.ROOT);
        String fullyDecodedUri = decodeFully(fullUriString).toLowerCase(Locale.ROOT);

        if (fullUriString.length() > MAX_URI_LENGTH) {
            log.warn("Blocked request with excessively long URI: length={}", fullUriString.length());
            return rejectWithProblemDetail(exchange, "Request URI exceeds maximum permissible length");
        }

        // Null-byte injection check
        if (lowerRawUri.contains("%00") || lowerRawUri.contains("\0") || lowerRawUri.contains("\\x00")
                || fullyDecodedUri.contains("\0") || fullyDecodedUri.contains("%00")) {
            log.warn("Blocked null-byte injection attempt: {}", fullUriString);
            return rejectWithProblemDetail(exchange, "Null byte character detected");
        }

        // Path traversal check
        if (lowerRawUri.contains("..") || lowerRawUri.contains("%2e%2e")
                || fullyDecodedUri.contains("..") || fullyDecodedUri.contains("/../") || fullyDecodedUri.contains("\\..\\")) {
            log.warn("Blocked path traversal attempt in URI: {}", fullUriString);
            return rejectWithProblemDetail(exchange, "Path traversal sequence detected");
        }

        // Script injection / XSS check
        if (lowerRawUri.contains("<script") || lowerRawUri.contains("%3cscript")
                || fullyDecodedUri.contains("<script") || fullyDecodedUri.contains("javascript:")
                || fullyDecodedUri.contains("onerror=") || fullyDecodedUri.contains("onload=")) {
            log.warn("Blocked XSS pattern in request: {}", fullUriString);
            return rejectWithProblemDetail(exchange, "Malicious script pattern detected in request");
        }

        // SQL injection signature check
        if (fullyDecodedUri.contains("union select") || lowerRawUri.contains("union+select")
                || lowerRawUri.contains("union%20select") || fullyDecodedUri.contains("benchmark(")
                || fullyDecodedUri.contains("waitfor delay")) {
            log.warn("Blocked SQL injection signature in request: {}", fullUriString);
            return rejectWithProblemDetail(exchange, "Disallowed SQL signature detected in query parameters");
        }

        return chain.filter(exchange);
    }

    private String decodeFully(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        String current = value;
        for (int i = 0; i < 3; i++) {
            try {
                String decoded = URLDecoder.decode(current, StandardCharsets.UTF_8);
                if (decoded.equals(current)) {
                    break;
                }
                current = decoded;
            } catch (Exception e) {
                break;
            }
        }
        return current;
    }

    private Mono<Void> rejectWithProblemDetail(ServerWebExchange exchange, String detail) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);

        String jsonBody = String.format(
                "{\"type\":\"https://api.eventpulse.com/errors/bad-request\",\"title\":\"Bad Request\",\"status\":400,\"detail\":\"%s\"}",
                detail
        );

        byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
        response.getHeaders().setContentLength(bytes.length);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -90;
    }
}
