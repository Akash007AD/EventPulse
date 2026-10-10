package com.eventpulse.booking;

import com.eventpulse.booking.client.EventCatalogClient;
import com.eventpulse.booking.dto.BookingResponse;
import com.eventpulse.booking.dto.CreateBookingRequest;
import com.eventpulse.booking.dto.EventSummaryDto;
import com.eventpulse.booking.dto.PagedResponse;
import com.eventpulse.booking.entity.BookingStatus;
import com.eventpulse.booking.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import com.eventpulse.booking.config.TestSecurityConfig;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class BookingIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookingRepository bookingRepository;

    @MockBean
    private EventCatalogClient eventCatalogClient;

    @BeforeEach
    void cleanUp() {
        bookingRepository.deleteAll();
    }

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/bookings";
    }

    @Test
    @DisplayName("End-to-End Booking Lifecycle: Create Pending -> Fetch -> Cancel")
    void bookingLifecycle_e2e() {
        UUID eventId = UUID.randomUUID();

        // 1. Mock Event Service response
        when(eventCatalogClient.getPublishedEvent(eventId)).thenReturn(
                Optional.of(new EventSummaryDto(eventId, "Tech Conference", "PUBLISHED", "INR"))
        );

        // 2. Create Booking
        CreateBookingRequest createRequest = new CreateBookingRequest(
                eventId, 2, new BigDecimal("998.00"), "INR", "user-john"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("mock-jwt-token");
        headers.set("X-Customer-Subject", "user-john");

        ResponseEntity<BookingResponse> createResponse = restTemplate.postForEntity(
                getBaseUrl(), new HttpEntity<>(createRequest, headers), BookingResponse.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        BookingResponse created = createResponse.getBody();
        assertThat(created).isNotNull();
        UUID bookingId = created.getId();
        assertThat(created.getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
        assertThat(created.getQuantity()).isEqualTo(2);

        // 3. Fetch Booking by ID
        ResponseEntity<BookingResponse> getResponse = restTemplate.exchange(
                getBaseUrl() + "/" + bookingId, HttpMethod.GET,
                new HttpEntity<>(headers), BookingResponse.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getId()).isEqualTo(bookingId);

        // 4. List my bookings
        ResponseEntity<PagedResponse<BookingResponse>> listResponse = restTemplate.exchange(
                getBaseUrl() + "/me", HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
        );
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).isNotNull();
        assertThat(listResponse.getBody().getTotalElements()).isEqualTo(1);

        // 5. Cancel Booking
        ResponseEntity<BookingResponse> cancelResponse = restTemplate.exchange(
                getBaseUrl() + "/" + bookingId + "/cancel", HttpMethod.POST,
                new HttpEntity<>(headers), BookingResponse.class
        );
        assertThat(cancelResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cancelResponse.getBody()).isNotNull();
        assertThat(cancelResponse.getBody().getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    @DisplayName("Actuator health endpoint should return UP status")
    void healthCheck_returnsUp() {
        ResponseEntity<String> healthResponse = restTemplate.getForEntity(
                "http://localhost:" + port + "/actuator/health", String.class
        );
        assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(healthResponse.getBody()).contains("\"status\":\"UP\"");
    }
}
