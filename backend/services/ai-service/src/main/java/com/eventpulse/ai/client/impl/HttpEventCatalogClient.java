package com.eventpulse.ai.client.impl;

import com.eventpulse.ai.client.EventCatalogClient;
import com.eventpulse.ai.dto.EventSummaryDto;
import com.eventpulse.ai.dto.SearchCriteriaDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HttpEventCatalogClient implements EventCatalogClient {

    private final RestClient restClient;

    public HttpEventCatalogClient(@Value("${eventpulse.event-service.url:http://localhost:8081}") String eventServiceUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(3000));
        factory.setReadTimeout(Duration.ofMillis(5000));

        this.restClient = RestClient.builder()
                .baseUrl(eventServiceUrl)
                .requestFactory(factory)
                .build();
    }

    @Override
    public List<EventSummaryDto> searchPublishedEvents(SearchCriteriaDto criteria) {
        try {
            var requestUri = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/api/v1/events");
                        if (criteria != null && criteria.getCity() != null && !criteria.getCity().isBlank()) {
                            uriBuilder.queryParam("city", criteria.getCity());
                        }
                        uriBuilder.queryParam("page", 0);
                        uriBuilder.queryParam("size", 20);
                        return uriBuilder.build();
                    });

            PagedEventResponse response = requestUri.retrieve()
                    .body(PagedEventResponse.class);

            if (response == null || response.getContent() == null) {
                return Collections.emptyList();
            }

            return response.getContent().stream()
                    .filter(e -> "PUBLISHED".equalsIgnoreCase(e.getStatus()))
                    .filter(e -> {
                        if (criteria != null && criteria.getMaxPrice() != null && e.getBasePrice() != null) {
                            return e.getBasePrice().compareTo(criteria.getMaxPrice()) <= 0;
                        }
                        return true;
                    })
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            log.warn("Failed to retrieve events from Event Service: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class PagedEventResponse {
        private List<EventSummaryDto> content;
        private int pageNumber;
        private int pageSize;
        private long totalElements;
    }
}
