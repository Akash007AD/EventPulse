package com.eventpulse.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class GatewaySecurityAndResilienceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("OWASP security response headers are present on gateway responses")
    void securityHeaders_areInjectedOnResponses() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("X-Frame-Options", "DENY")
                .expectHeader().valueEquals("X-XSS-Protection", "1; mode=block")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin")
                .expectHeader().valueEquals("Content-Security-Policy", "default-src 'self'");
    }

    @Test
    @DisplayName("Path traversal attack is blocked with HTTP 400 Bad Request ProblemDetail")
    void maliciousGuard_blocksPathTraversal() {
        webTestClient.get()
                .uri("/api/v1/events/../../etc/passwd")
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type").isEqualTo("https://api.eventpulse.com/errors/bad-request")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.detail").value(val -> assertThat(val.toString()).contains("Path traversal"));
    }

    @Test
    @DisplayName("Null byte injection attack is blocked with HTTP 400 Bad Request ProblemDetail")
    void maliciousGuard_blocksNullByteInjection() {
        webTestClient.get()
                .uri("/api/v1/events/%00admin")
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type").isEqualTo("https://api.eventpulse.com/errors/bad-request")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.detail").value(val -> assertThat(val.toString()).contains("Null byte"));
    }

    @Test
    @DisplayName("Cross-Site Scripting attack in query string is blocked with HTTP 400 Bad Request ProblemDetail")
    void maliciousGuard_blocksXssInQueryString() {
        webTestClient.get()
                .uri("/api/v1/events?search=%3Cscript%3Ealert('xss')%3C/script%3E")
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type").isEqualTo("https://api.eventpulse.com/errors/bad-request")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.detail").value(val -> assertThat(val.toString()).contains("Malicious script pattern"));
    }

    @Test
    @DisplayName("SQL injection signature in query string is blocked with HTTP 400 Bad Request ProblemDetail")
    void maliciousGuard_blocksSqlInjectionInQuery() {
        webTestClient.get()
                .uri("/api/v1/events?city=London%20union%20select%201,2,3")
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type").isEqualTo("https://api.eventpulse.com/errors/bad-request")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.detail").value(val -> assertThat(val.toString()).contains("SQL signature"));
    }

    @Test
    @DisplayName("Circuit breaker fallback endpoint returns HTTP 503 ProblemDetail")
    void fallbackEndpoint_returnsServiceUnavailable() {
        webTestClient.get()
                .uri("/fallback")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type").isEqualTo("https://api.eventpulse.com/errors/service-unavailable")
                .jsonPath("$.status").isEqualTo(503)
                .jsonPath("$.title").isEqualTo("Service Unavailable");
    }

    @Test
    @DisplayName("High-frequency requests trigger HTTP 429 Too Many Requests with Retry-After header")
    void rateLimiter_triggersTooManyRequestsOnBurst() {
        // Payment tier has burst capacity of 10.
        // Send requests until rate limiter consumes all tokens and returns 429.
        boolean rateLimited = false;

        for (int i = 0; i < 15; i++) {
            var result = webTestClient.get()
                    .uri("/api/v1/payments/rate-limit-test-" + i)
                    .exchange()
                    .expectBody()
                    .returnResult();

            if (result.getStatus().value() == 429) {
                rateLimited = true;
                assertThat(result.getResponseHeaders().getFirst(HttpHeaders.RETRY_AFTER)).isEqualTo("60");
                break;
            }
        }

        assertThat(rateLimited)
                .as("Rate limiter should have triggered HTTP 429 after exceeding burst capacity")
                .isTrue();
    }
}
