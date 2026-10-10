package com.eventpulse.analytics.service.impl;

import com.eventpulse.analytics.dto.EventAnalyticsResponse;
import com.eventpulse.analytics.dto.PlatformOverviewResponse;
import com.eventpulse.analytics.entity.EventAnalytics;
import com.eventpulse.analytics.entity.ProcessedEvent;
import com.eventpulse.analytics.exception.EventAnalyticsNotFoundException;
import com.eventpulse.analytics.repository.EventAnalyticsRepository;
import com.eventpulse.analytics.repository.ProcessedEventRepository;
import com.eventpulse.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final EventAnalyticsRepository analyticsRepository;
    private final ProcessedEventRepository processedEventRepository;

    @Override
    public void processBookingConfirmed(UUID eventId, String eventTitle, int quantity, BigDecimal amount, String currency, String messageId) {
        log.info("Processing BookingConfirmed for event: {}, quantity: {}, messageId: {}", eventId, quantity, messageId);

        // 1. Idempotency deduplication check
        if (messageId != null && processedEventRepository.existsById(messageId)) {
            log.info("Message {} already processed. Skipping duplicate projection.", messageId);
            return;
        }

        // 2. Fetch or create EventAnalytics aggregate
        EventAnalytics analytics = analyticsRepository.findByEventId(eventId)
                .orElseGet(() -> EventAnalytics.builder()
                        .id(UUID.randomUUID())
                        .eventId(eventId)
                        .eventTitle(eventTitle != null ? eventTitle : "Event " + eventId)
                        .totalRevenue(BigDecimal.ZERO)
                        .currency(currency != null ? currency.toUpperCase() : "INR")
                        .ticketsSold(0)
                        .confirmedBookings(0)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build());

        if (eventTitle != null && !eventTitle.isBlank()) {
            analytics.setEventTitle(eventTitle);
        }
        analytics.setTicketsSold(analytics.getTicketsSold() + quantity);
        analytics.setConfirmedBookings(analytics.getConfirmedBookings() + 1);
        if (amount != null) {
            analytics.setTotalRevenue(analytics.getTotalRevenue().add(amount));
        }

        analyticsRepository.save(analytics);

        // 3. Record processed event
        if (messageId != null) {
            processedEventRepository.save(ProcessedEvent.builder()
                    .eventId(messageId)
                    .eventType("BookingConfirmed")
                    .processedAt(Instant.now())
                    .build());
        }

        log.info("Successfully updated projection for event {}: ticketsSold={}, totalRevenue={}",
                eventId, analytics.getTicketsSold(), analytics.getTotalRevenue());
    }

    @Override
    public void processPaymentSucceeded(UUID eventId, BigDecimal amount, String currency, String messageId) {
        log.info("Processing PaymentSucceeded for event: {}, amount: {}, messageId: {}", eventId, amount, messageId);

        if (messageId != null && processedEventRepository.existsById(messageId)) {
            log.info("Message {} already processed. Skipping duplicate payment projection.", messageId);
            return;
        }

        EventAnalytics analytics = analyticsRepository.findByEventId(eventId)
                .orElseGet(() -> EventAnalytics.builder()
                        .id(UUID.randomUUID())
                        .eventId(eventId)
                        .eventTitle("Event " + eventId)
                        .totalRevenue(BigDecimal.ZERO)
                        .currency(currency != null ? currency.toUpperCase() : "INR")
                        .ticketsSold(0)
                        .confirmedBookings(0)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build());

        if (amount != null) {
            analytics.setTotalRevenue(analytics.getTotalRevenue().add(amount));
        }
        analyticsRepository.save(analytics);

        if (messageId != null) {
            processedEventRepository.save(ProcessedEvent.builder()
                    .eventId(messageId)
                    .eventType("PaymentSucceeded")
                    .processedAt(Instant.now())
                    .build());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EventAnalyticsResponse getEventAnalytics(UUID eventId) {
        EventAnalytics analytics = analyticsRepository.findByEventId(eventId)
                .orElseThrow(() -> new EventAnalyticsNotFoundException(eventId));
        return EventAnalyticsResponse.fromEntity(analytics);
    }

    @Override
    @Transactional(readOnly = true)
    public PlatformOverviewResponse getPlatformOverview() {
        BigDecimal totalRevenue = analyticsRepository.sumTotalRevenue();
        Long totalTickets = analyticsRepository.sumTotalTicketsSold();
        Long totalBookings = analyticsRepository.sumTotalConfirmedBookings();
        long totalEvents = analyticsRepository.count();

        return PlatformOverviewResponse.builder()
                .totalGrossRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .currency("INR")
                .totalTicketsSold(totalTickets != null ? totalTickets : 0L)
                .totalConfirmedBookings(totalBookings != null ? totalBookings : 0L)
                .totalTrackedEvents(totalEvents)
                .build();
    }
}
