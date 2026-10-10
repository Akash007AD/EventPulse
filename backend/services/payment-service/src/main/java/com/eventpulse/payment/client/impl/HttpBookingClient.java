package com.eventpulse.payment.client.impl;

import com.eventpulse.payment.client.BookingClient;
import com.eventpulse.payment.dto.BookingDetailsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class HttpBookingClient implements BookingClient {

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 150L;

    private final RestClient restClient;

    public HttpBookingClient(
            RestClient.Builder restClientBuilder,
            @Value("${booking.service.url:http://localhost:8082}") String bookingServiceUrl) {
        log.info("Configuring HttpBookingClient pointing to: {}", bookingServiceUrl);
        this.restClient = restClientBuilder
                .baseUrl(bookingServiceUrl)
                .build();
    }

    @Override
    public Optional<BookingDetailsDto> getBooking(UUID bookingId) {
        long backoff = INITIAL_BACKOFF_MS;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                BookingDetailsDto booking = restClient.get()
                        .uri("/api/v1/bookings/{id}", bookingId)
                        .retrieve()
                        .onStatus(status -> status.value() == 404, (req, resp) -> {
                            log.warn("Booking {} not found in Booking Service", bookingId);
                        })
                        .body(BookingDetailsDto.class);
                return Optional.ofNullable(booking);
            } catch (HttpClientErrorException.NotFound ex) {
                // Do not retry 404 Not Found
                return Optional.empty();
            } catch (Exception ex) {
                if (attempt == MAX_RETRIES) {
                    log.warn("Failed to retrieve booking {} from Booking Service after {} attempts: {}",
                            bookingId, attempt, ex.getMessage());
                    return Optional.empty();
                }
                log.warn("Attempt {}/{} failed to retrieve booking {}: {}. Retrying in {}ms...",
                        attempt, MAX_RETRIES, bookingId, ex.getMessage(), backoff);
                sleepQuietly(backoff);
                backoff *= 2;
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean confirmBooking(UUID bookingId) {
        long backoff = INITIAL_BACKOFF_MS;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                restClient.post()
                        .uri("/api/v1/bookings/{id}/confirm", bookingId)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, (req, resp) -> {
                            log.error("Failed to confirm booking {} in Booking Service. HTTP status: {}",
                                    bookingId, resp.getStatusCode());
                        })
                        .toBodilessEntity();
                log.info("Successfully confirmed booking {} in Booking Service (attempt {})", bookingId, attempt);
                return true;
            } catch (HttpClientErrorException ex) {
                // Client error (4xx) should not be blindly retried
                log.error("Client error confirming booking {} (HTTP {}): {}", bookingId, ex.getStatusCode(), ex.getMessage());
                return false;
            } catch (Exception ex) {
                if (attempt == MAX_RETRIES) {
                    log.error("Error communicating confirmation for booking {} after {} attempts: {}",
                            bookingId, attempt, ex.getMessage());
                    return false;
                }
                log.warn("Attempt {}/{} failed to confirm booking {}: {}. Retrying in {}ms...",
                        attempt, MAX_RETRIES, bookingId, ex.getMessage(), backoff);
                sleepQuietly(backoff);
                backoff *= 2;
            }
        }
        return false;
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
