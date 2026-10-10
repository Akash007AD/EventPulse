package com.eventpulse.payment.client.impl;

import com.eventpulse.payment.client.BookingClient;
import com.eventpulse.payment.dto.BookingDetailsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class HttpBookingClient implements BookingClient {

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
        try {
            BookingDetailsDto booking = restClient.get()
                    .uri("/api/v1/bookings/{id}", bookingId)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (req, resp) -> {
                        log.warn("Booking {} not found in Booking Service", bookingId);
                    })
                    .body(BookingDetailsDto.class);
            return Optional.ofNullable(booking);
        } catch (Exception ex) {
            log.warn("Failed to retrieve booking {} from Booking Service: {}", bookingId, ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean confirmBooking(UUID bookingId) {
        try {
            restClient.post()
                    .uri("/api/v1/bookings/{id}/confirm", bookingId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, resp) -> {
                        log.error("Failed to confirm booking {} in Booking Service. HTTP status: {}", bookingId, resp.getStatusCode());
                    })
                    .toBodilessEntity();
            log.info("Successfully confirmed booking {} in Booking Service", bookingId);
            return true;
        } catch (Exception ex) {
            log.error("Error communicating confirmation for booking {}: {}", bookingId, ex.getMessage());
            return false;
        }
    }
}
