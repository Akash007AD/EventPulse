package com.eventpulse.analytics;

import com.eventpulse.analytics.config.TestSecurityConfig;
import com.eventpulse.analytics.entity.EventAnalytics;
import com.eventpulse.analytics.entity.ProcessedEvent;
import com.eventpulse.analytics.repository.EventAnalyticsRepository;
import com.eventpulse.analytics.repository.ProcessedEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class AnalyticsIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EventAnalyticsRepository analyticsRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @BeforeEach
    void cleanUp() {
        processedEventRepository.deleteAll();
        analyticsRepository.deleteAll();
    }

    @Test
    @DisplayName("Actuator health endpoint should return UP")
    void healthCheck_returnsUp() {
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/actuator/health", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }

    @Test
    @DisplayName("Repositories should persist and retrieve EventAnalytics and ProcessedEvent records")
    void persistence_integration() {
        UUID eventId = UUID.randomUUID();
        EventAnalytics analytics = EventAnalytics.builder()
                .id(UUID.randomUUID())
                .eventId(eventId)
                .eventTitle("Cloud Native Day")
                .totalRevenue(new BigDecimal("75000.00"))
                .currency("INR")
                .ticketsSold(150)
                .confirmedBookings(80)
                .build();

        analyticsRepository.save(analytics);

        Optional<EventAnalytics> retrieved = analyticsRepository.findByEventId(eventId);
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getTicketsSold()).isEqualTo(150);

        processedEventRepository.save(ProcessedEvent.builder()
                .eventId("event-msg-456")
                .eventType("BookingConfirmed")
                .processedAt(Instant.now())
                .build());

        assertThat(processedEventRepository.existsById("event-msg-456")).isTrue();
    }
}
