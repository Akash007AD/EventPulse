package com.eventpulse.booking.client.impl;

import com.eventpulse.booking.client.EventCatalogClient;
import com.eventpulse.booking.dto.EventSummaryDto;
import com.eventpulse.booking.exception.EventValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class HttpEventCatalogClient implements EventCatalogClient {

    private final RestClient restClient;

    public HttpEventCatalogClient(
            @Value("${event-service.base-url}") String baseUrl,
            @Value("${event-service.connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${event-service.read-timeout-ms:5000}") int readTimeoutMs
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public Optional<EventSummaryDto> getPublishedEvent(UUID eventId) {
        log.debug("Verifying event existence in Event Service: {}", eventId);
        try {
            EventSummaryDto summary = restClient.get()
                    .uri("/api/v1/events/{id}", eventId)
                    .retrieve()
                    .body(EventSummaryDto.class);
            return Optional.ofNullable(summary);
        } catch (HttpClientErrorException.NotFound ex) {
            log.warn("Event {} not found or unpublished in Event Service", eventId);
            return Optional.empty();
        } catch (Exception ex) {
            log.error("Failed to connect to Event Service for event {}: {}", eventId, ex.getMessage());
            throw new EventValidationException("Failed to validate event with Event Service: " + ex.getMessage());
        }
    }
}
