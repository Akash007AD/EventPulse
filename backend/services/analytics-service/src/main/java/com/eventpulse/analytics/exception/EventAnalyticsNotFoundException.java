package com.eventpulse.analytics.exception;

import java.util.UUID;

public class EventAnalyticsNotFoundException extends RuntimeException {

    public EventAnalyticsNotFoundException(UUID eventId) {
        super("Analytics record not found for event: " + eventId);
    }
}
