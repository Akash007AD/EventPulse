package com.eventpulse.booking.client;

import com.eventpulse.booking.dto.EventSummaryDto;

import java.util.Optional;
import java.util.UUID;

public interface EventCatalogClient {

    Optional<EventSummaryDto> getPublishedEvent(UUID eventId);
}
