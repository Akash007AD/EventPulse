package com.eventpulse.event.service.impl;

import com.eventpulse.event.dto.CreateEventRequest;
import com.eventpulse.event.dto.EventResponse;
import com.eventpulse.event.dto.PagedResponse;
import com.eventpulse.event.dto.UpdateEventRequest;
import com.eventpulse.event.entity.Event;
import com.eventpulse.event.entity.EventStatus;
import com.eventpulse.event.exception.EventNotFoundException;
import com.eventpulse.event.exception.InvalidEventStateException;
import com.eventpulse.event.repository.EventRepository;
import com.eventpulse.event.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private static final int MAX_PAGE_SIZE = 100;

    private final EventRepository eventRepository;

    @Override
    public EventResponse createEvent(CreateEventRequest request) {
        log.info("Creating event: name='{}', city='{}'", request.getName(), request.getCity());

        EventStatus initialStatus = request.getStatus() != null ? request.getStatus() : EventStatus.DRAFT;

        Event event = Event.builder()
                .id(UUID.randomUUID())
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .venueName(request.getVenueName().trim())
                .city(request.getCity().trim())
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .currency(request.getCurrency().trim().toUpperCase())
                .status(initialStatus)
                .organizerSubject(request.getOrganizerSubject())
                .build();

        Event savedEvent = eventRepository.save(event);
        log.info("Event created successfully with ID: {}", savedEvent.getId());
        return EventResponse.fromEntity(savedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EventResponse> getPublishedEvents(int page, int size, String city) {
        int boundedPage = Math.max(page, 0);
        int boundedSize = (size <= 0 || size > MAX_PAGE_SIZE) ? 20 : size;

        Pageable pageable = PageRequest.of(boundedPage, boundedSize, Sort.by("startsAt").ascending());

        Page<Event> eventPage;
        if (city != null && !city.trim().isBlank()) {
            eventPage = eventRepository.findByStatusAndCityIgnoreCase(EventStatus.PUBLISHED, city.trim(), pageable);
        } else {
            eventPage = eventRepository.findByStatus(EventStatus.PUBLISHED, pageable);
        }

        return PagedResponse.of(eventPage, EventResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getPublishedEventById(UUID id) {
        log.debug("Fetching published event with ID: {}", id);
        return eventRepository.findByIdAndStatus(id, EventStatus.PUBLISHED)
                .map(EventResponse::fromEntity)
                .orElseThrow(() -> new EventNotFoundException(id));
    }

    @Override
    public EventResponse updateEvent(UUID id, UpdateEventRequest request) {
        log.info("Updating event with ID: {}", id);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));

        if (event.getStatus() == EventStatus.ARCHIVED || event.getStatus() == EventStatus.CANCELLED) {
            throw new InvalidEventStateException("Cannot update event in " + event.getStatus() + " status");
        }

        event.setName(request.getName().trim());
        event.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        event.setVenueName(request.getVenueName().trim());
        event.setCity(request.getCity().trim());
        event.setStartsAt(request.getStartsAt());
        event.setEndsAt(request.getEndsAt());
        event.setCurrency(request.getCurrency().trim().toUpperCase());

        if (request.getStatus() != null) {
            event.setStatus(request.getStatus());
        }

        Event updated = eventRepository.save(event);
        log.info("Event {} updated successfully", id);
        return EventResponse.fromEntity(updated);
    }

    @Override
    public EventResponse archiveEvent(UUID id) {
        log.info("Archiving event with ID: {}", id);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));

        event.setStatus(EventStatus.ARCHIVED);
        Event archived = eventRepository.save(event);
        log.info("Event {} archived successfully", id);
        return EventResponse.fromEntity(archived);
    }
}
