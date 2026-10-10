package com.eventpulse.event.service;

import com.eventpulse.event.dto.CreateEventRequest;
import com.eventpulse.event.dto.EventResponse;
import com.eventpulse.event.dto.PagedResponse;
import com.eventpulse.event.dto.UpdateEventRequest;

import java.util.UUID;

public interface EventService {

    EventResponse createEvent(CreateEventRequest request);

    PagedResponse<EventResponse> getPublishedEvents(int page, int size, String city);

    EventResponse getPublishedEventById(UUID id);

    EventResponse updateEvent(UUID id, UpdateEventRequest request);

    EventResponse updateEvent(UUID id, UpdateEventRequest request, String callerSubject, boolean isAdmin);

    EventResponse archiveEvent(UUID id);

    EventResponse archiveEvent(UUID id, String callerSubject, boolean isAdmin);
}
