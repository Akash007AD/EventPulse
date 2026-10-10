package com.eventpulse.analytics.service;

import com.eventpulse.analytics.dto.EventAnalyticsResponse;
import com.eventpulse.analytics.dto.PlatformOverviewResponse;

import java.math.BigDecimal;
import java.util.UUID;

public interface AnalyticsService {

    void processBookingConfirmed(UUID eventId, String eventTitle, int quantity, BigDecimal amount, String currency, String messageId);

    void processPaymentSucceeded(UUID eventId, BigDecimal amount, String currency, String messageId);

    EventAnalyticsResponse getEventAnalytics(UUID eventId);

    PlatformOverviewResponse getPlatformOverview();
}
