package com.eventpulse.event;

import com.eventpulse.event.config.TestSecurityConfig;
import com.eventpulse.event.dto.CreateEventRequest;
import com.eventpulse.event.dto.EventResponse;
import com.eventpulse.event.dto.UpdateEventRequest;
import com.eventpulse.event.entity.Event;
import com.eventpulse.event.entity.EventStatus;
import com.eventpulse.event.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class EventIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EventRepository eventRepository;

    @BeforeEach
    void cleanUp() {
        eventRepository.deleteAll();
    }

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/events";
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("mock-jwt-token");
        return headers;
    }

    @Test
    @DisplayName("Complete Event Lifecycle: Create -> Publish -> Fetch -> Update -> Archive")
    void eventLifecycle_e2e() {
        Instant startsAt = Instant.now().plus(7, ChronoUnit.DAYS);
        Instant endsAt = startsAt.plus(5, ChronoUnit.HOURS);

        // 1. Create Event (DRAFT) with Bearer token
        CreateEventRequest createRequest = new CreateEventRequest(
                "Java Community Conference",
                "Full-day Java and Spring Boot summit",
                "Biswa Bangla Mela Prangan",
                "Kolkata",
                startsAt,
                endsAt,
                "INR",
                EventStatus.DRAFT,
                "org-user-42"
        );

        HttpEntity<CreateEventRequest> createEntity = new HttpEntity<>(createRequest, createAuthHeaders());
        ResponseEntity<EventResponse> createResponse = restTemplate.postForEntity(
                getBaseUrl(), createEntity, EventResponse.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        EventResponse createdBody = createResponse.getBody();
        assertThat(createdBody).isNotNull();
        UUID eventId = createdBody.getId();
        assertThat(createdBody.getName()).isEqualTo("Java Community Conference");
        assertThat(createdBody.getStatus()).isEqualTo(EventStatus.DRAFT);

        // 2. Draft should not appear in public published list (unauthenticated)
        ResponseEntity<String> publicListResponse = restTemplate.getForEntity(
                getBaseUrl(), String.class
        );
        assertThat(publicListResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(publicListResponse.getBody()).contains("\"totalElements\":0");

        // 3. Update Event to PUBLISHED status with Bearer token
        UpdateEventRequest updateRequest = new UpdateEventRequest(
                "Java Community Conference 2027",
                "Updated full-day summit description",
                "Biswa Bangla Mela Prangan - Main Hall",
                "Kolkata",
                startsAt,
                endsAt,
                "INR",
                EventStatus.PUBLISHED
        );

        ResponseEntity<EventResponse> updateResponse = restTemplate.exchange(
                getBaseUrl() + "/" + eventId,
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest, createAuthHeaders()),
                EventResponse.class
        );

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().getStatus()).isEqualTo(EventStatus.PUBLISHED);
        assertThat(updateResponse.getBody().getName()).isEqualTo("Java Community Conference 2027");

        // 4. Public list should now contain the event (unauthenticated)
        ResponseEntity<String> publishedListResponse = restTemplate.getForEntity(
                getBaseUrl() + "?city=Kolkata", String.class
        );
        assertThat(publishedListResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(publishedListResponse.getBody()).contains("Java Community Conference 2027");
        assertThat(publishedListResponse.getBody()).contains("\"totalElements\":1");

        // 5. Direct get by ID (unauthenticated public lookup)
        ResponseEntity<EventResponse> getByIdResponse = restTemplate.getForEntity(
                getBaseUrl() + "/" + eventId, EventResponse.class
        );
        assertThat(getByIdResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getByIdResponse.getBody()).isNotNull();
        assertThat(getByIdResponse.getBody().getId()).isEqualTo(eventId);

        // 6. Archive/soft-deactivate the event with Bearer token
        ResponseEntity<EventResponse> archiveResponse = restTemplate.exchange(
                getBaseUrl() + "/" + eventId,
                HttpMethod.DELETE,
                new HttpEntity<>(createAuthHeaders()),
                EventResponse.class
        );
        assertThat(archiveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(archiveResponse.getBody()).isNotNull();
        assertThat(archiveResponse.getBody().getStatus()).isEqualTo(EventStatus.ARCHIVED);

        // 7. Verify soft deletion: entity is still present in database with ARCHIVED status
        Optional<Event> dbEntity = eventRepository.findById(eventId);
        assertThat(dbEntity).isPresent();
        assertThat(dbEntity.get().getStatus()).isEqualTo(EventStatus.ARCHIVED);

        // 8. Public lookup should now return 404 because it is no longer published
        ResponseEntity<String> getArchivedResponse = restTemplate.getForEntity(
                getBaseUrl() + "/" + eventId, String.class
        );
        assertThat(getArchivedResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Actuator health endpoint should return UP status without authentication")
    void healthCheck_returnsUp() {
        ResponseEntity<String> healthResponse = restTemplate.getForEntity(
                "http://localhost:" + port + "/actuator/health", String.class
        );
        assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(healthResponse.getBody()).contains("\"status\":\"UP\"");
    }
}
