package com.eventpulse.event.service;

import com.eventpulse.event.dto.CreateEventRequest;
import com.eventpulse.event.dto.EventResponse;
import com.eventpulse.event.dto.PagedResponse;
import com.eventpulse.event.dto.UpdateEventRequest;
import com.eventpulse.event.entity.Event;
import com.eventpulse.event.entity.EventStatus;
import com.eventpulse.event.exception.EventNotFoundException;
import com.eventpulse.event.exception.InvalidEventStateException;
import com.eventpulse.event.repository.EventRepository;
import com.eventpulse.event.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    private EventServiceImpl eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventServiceImpl(eventRepository);
    }

    @Test
    @DisplayName("createEvent should persist event and default to DRAFT when status not provided")
    void createEvent_shouldDefaultToDraft() {
        Instant now = Instant.now();
        CreateEventRequest request = new CreateEventRequest(
                "Tech Conference 2027",
                "Developer summit",
                "Biswa Bangla Convention Centre",
                "Kolkata",
                now.plus(10, ChronoUnit.DAYS),
                now.plus(11, ChronoUnit.DAYS),
                "INR",
                null,
                "organizer-123"
        );

        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventResponse response = eventService.createEvent(request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Tech Conference 2027");
        assertThat(response.getCity()).isEqualTo("Kolkata");
        assertThat(response.getStatus()).isEqualTo(EventStatus.DRAFT);
        assertThat(response.getCurrency()).isEqualTo("INR");

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(EventStatus.DRAFT);
    }

    @Test
    @DisplayName("getPublishedEvents should query only PUBLISHED events and filter by city when provided")
    void getPublishedEvents_withCityFilter() {
        Instant now = Instant.now();
        Event event = new Event(
                UUID.randomUUID(),
                "Kolkata DevFest",
                "Desc",
                "Venue 1",
                "Kolkata",
                now.plus(5, ChronoUnit.DAYS),
                now.plus(6, ChronoUnit.DAYS),
                "INR",
                EventStatus.PUBLISHED,
                "org-1"
        );

        when(eventRepository.findByStatusAndCityIgnoreCase(eq(EventStatus.PUBLISHED), eq("Kolkata"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(event)));

        PagedResponse<EventResponse> response = eventService.getPublishedEvents(0, 10, "Kolkata");

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getName()).isEqualTo("Kolkata DevFest");
        assertThat(response.getContent().get(0).getCity()).isEqualTo("Kolkata");
        verify(eventRepository).findByStatusAndCityIgnoreCase(eq(EventStatus.PUBLISHED), eq("Kolkata"), any(Pageable.class));
    }

    @Test
    @DisplayName("getPublishedEventById should return event when published")
    void getPublishedEventById_success() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Event event = new Event(
                id,
                "Spring Boot Masterclass",
                "Desc",
                "Online Hall",
                "Bengaluru",
                now.plus(1, ChronoUnit.DAYS),
                now.plus(2, ChronoUnit.DAYS),
                "INR",
                EventStatus.PUBLISHED,
                "org-2"
        );

        when(eventRepository.findByIdAndStatus(id, EventStatus.PUBLISHED)).thenReturn(Optional.of(event));

        EventResponse response = eventService.getPublishedEventById(id);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getName()).isEqualTo("Spring Boot Masterclass");
    }

    @Test
    @DisplayName("getPublishedEventById should throw EventNotFoundException when event is draft or not found")
    void getPublishedEventById_notFound() {
        UUID id = UUID.randomUUID();
        when(eventRepository.findByIdAndStatus(id, EventStatus.PUBLISHED)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getPublishedEventById(id))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("updateEvent should update fields and save")
    void updateEvent_success() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Event existing = new Event(
                id,
                "Old Title",
                "Old Desc",
                "Old Venue",
                "Delhi",
                now.plus(2, ChronoUnit.DAYS),
                now.plus(3, ChronoUnit.DAYS),
                "INR",
                EventStatus.DRAFT,
                "org-1"
        );

        UpdateEventRequest updateRequest = new UpdateEventRequest(
                "New Title",
                "New Desc",
                "New Venue",
                "Mumbai",
                now.plus(3, ChronoUnit.DAYS),
                now.plus(4, ChronoUnit.DAYS),
                "INR",
                EventStatus.PUBLISHED
        );

        when(eventRepository.findById(id)).thenReturn(Optional.of(existing));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        EventResponse response = eventService.updateEvent(id, updateRequest);

        assertThat(response.getName()).isEqualTo("New Title");
        assertThat(response.getCity()).isEqualTo("Mumbai");
        assertThat(response.getStatus()).isEqualTo(EventStatus.PUBLISHED);
    }

    @Test
    @DisplayName("updateEvent should throw InvalidEventStateException if event is ARCHIVED")
    void updateEvent_archivedConflict() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Event archived = new Event(
                id,
                "Archived Event",
                "Desc",
                "Venue",
                "Delhi",
                now.plus(2, ChronoUnit.DAYS),
                now.plus(3, ChronoUnit.DAYS),
                "INR",
                EventStatus.ARCHIVED,
                "org-1"
        );

        UpdateEventRequest updateRequest = new UpdateEventRequest(
                "New Title", "Desc", "Venue", "Delhi",
                now.plus(2, ChronoUnit.DAYS), now.plus(3, ChronoUnit.DAYS), "INR", EventStatus.DRAFT
        );

        when(eventRepository.findById(id)).thenReturn(Optional.of(archived));

        assertThatThrownBy(() -> eventService.updateEvent(id, updateRequest))
                .isInstanceOf(InvalidEventStateException.class)
                .hasMessageContaining("Cannot update event in ARCHIVED status");
    }

    @Test
    @DisplayName("archiveEvent should soft-deactivate by setting status to ARCHIVED without deleting the row")
    void archiveEvent_shouldSetStatusToArchived() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Event event = new Event(
                id,
                "Active Event",
                "Desc",
                "Venue",
                "Pune",
                now.plus(5, ChronoUnit.DAYS),
                now.plus(6, ChronoUnit.DAYS),
                "INR",
                EventStatus.PUBLISHED,
                "org-1"
        );

        when(eventRepository.findById(id)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        EventResponse response = eventService.archiveEvent(id);

        assertThat(response.getStatus()).isEqualTo(EventStatus.ARCHIVED);
        verify(eventRepository).save(event);
        assertThat(event.getStatus()).isEqualTo(EventStatus.ARCHIVED);
    }
}
