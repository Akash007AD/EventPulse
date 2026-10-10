package com.eventpulse.analytics.service;

import com.eventpulse.analytics.dto.EventAnalyticsResponse;
import com.eventpulse.analytics.dto.PlatformOverviewResponse;
import com.eventpulse.analytics.entity.EventAnalytics;
import com.eventpulse.analytics.entity.ProcessedEvent;
import com.eventpulse.analytics.repository.EventAnalyticsRepository;
import com.eventpulse.analytics.repository.ProcessedEventRepository;
import com.eventpulse.analytics.service.impl.AnalyticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private EventAnalyticsRepository analyticsRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    private AnalyticsServiceImpl analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsServiceImpl(analyticsRepository, processedEventRepository);
    }

    @Test
    @DisplayName("processBookingConfirmed should create and save new aggregate when not exists")
    void processBookingConfirmed_createsNewAggregate() {
        UUID eventId = UUID.randomUUID();
        when(processedEventRepository.existsById("msg-100")).thenReturn(false);
        when(analyticsRepository.findByEventId(eventId)).thenReturn(Optional.empty());
        when(analyticsRepository.save(any(EventAnalytics.class))).thenAnswer(inv -> inv.getArgument(0));

        analyticsService.processBookingConfirmed(
                eventId, "Tech Conference", 2, new BigDecimal("1200.00"), "INR", "msg-100"
        );

        ArgumentCaptor<EventAnalytics> captor = ArgumentCaptor.forClass(EventAnalytics.class);
        verify(analyticsRepository).save(captor.capture());
        EventAnalytics saved = captor.getValue();

        assertThat(saved.getEventId()).isEqualTo(eventId);
        assertThat(saved.getTicketsSold()).isEqualTo(2);
        assertThat(saved.getConfirmedBookings()).isEqualTo(1);
        assertThat(saved.getTotalRevenue()).isEqualByComparingTo("1200.00");

        verify(processedEventRepository).save(any(ProcessedEvent.class));
    }

    @Test
    @DisplayName("processBookingConfirmed should skip duplicate projection when messageId was already processed")
    void processBookingConfirmed_idempotentDeduplication() {
        UUID eventId = UUID.randomUUID();
        when(processedEventRepository.existsById("msg-duplicate")).thenReturn(true);

        analyticsService.processBookingConfirmed(
                eventId, "Tech Conference", 2, new BigDecimal("1200.00"), "INR", "msg-duplicate"
        );

        verify(analyticsRepository, never()).save(any());
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("processPaymentSucceeded should increment total revenue for existing event")
    void processPaymentSucceeded_updatesRevenue() {
        UUID eventId = UUID.randomUUID();
        EventAnalytics existing = EventAnalytics.builder()
                .id(UUID.randomUUID())
                .eventId(eventId)
                .eventTitle("Spring Summit")
                .totalRevenue(new BigDecimal("1000.00"))
                .currency("INR")
                .ticketsSold(2)
                .confirmedBookings(1)
                .build();

        when(processedEventRepository.existsById("msg-pay-1")).thenReturn(false);
        when(analyticsRepository.findByEventId(eventId)).thenReturn(Optional.of(existing));
        when(analyticsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        analyticsService.processPaymentSucceeded(eventId, new BigDecimal("500.00"), "INR", "msg-pay-1");

        assertThat(existing.getTotalRevenue()).isEqualByComparingTo("1500.00");
        verify(analyticsRepository).save(existing);
    }

    @Test
    @DisplayName("getPlatformOverview should aggregate metrics across all events")
    void getPlatformOverview_aggregatesData() {
        when(analyticsRepository.sumTotalRevenue()).thenReturn(new BigDecimal("250000.00"));
        when(analyticsRepository.sumTotalTicketsSold()).thenReturn(450L);
        when(analyticsRepository.sumTotalConfirmedBookings()).thenReturn(210L);
        when(analyticsRepository.count()).thenReturn(15L);

        PlatformOverviewResponse overview = analyticsService.getPlatformOverview();

        assertThat(overview.getTotalGrossRevenue()).isEqualByComparingTo("250000.00");
        assertThat(overview.getTotalTicketsSold()).isEqualTo(450L);
        assertThat(overview.getTotalConfirmedBookings()).isEqualTo(210L);
        assertThat(overview.getTotalTrackedEvents()).isEqualTo(15L);
    }
}
