package com.eventpulse.event.service;

import com.eventpulse.event.config.RedisCacheConfig;
import com.eventpulse.event.dto.EventResponse;
import com.eventpulse.event.dto.UpdateEventRequest;
import com.eventpulse.event.entity.Event;
import com.eventpulse.event.entity.EventStatus;
import com.eventpulse.event.repository.EventRepository;
import com.eventpulse.event.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {EventServiceImpl.class})
@ActiveProfiles("test")
@Import(RedisCacheConfig.class)
class EventCacheTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void clearCache() {
        cacheManager.getCacheNames().forEach(name -> {
            var cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        });
    }

    @Test
    @DisplayName("getPublishedEventById caches the result on first invocation and serves subsequent calls from cache")
    void getPublishedEventById_cachesResult() {
        UUID eventId = UUID.randomUUID();
        Event event = Event.builder()
                .id(eventId)
                .name("Coldplay Live")
                .venueName("DY Patil Stadium")
                .city("Mumbai")
                .startsAt(Instant.now().plusSeconds(86400))
                .endsAt(Instant.now().plusSeconds(97200))
                .currency("INR")
                .status(EventStatus.PUBLISHED)
                .organizerSubject("org-1")
                .build();

        when(eventRepository.findByIdAndStatus(eq(eventId), eq(EventStatus.PUBLISHED)))
                .thenReturn(Optional.of(event));

        // 1st call -> hits repository
        EventResponse first = eventService.getPublishedEventById(eventId);
        assertThat(first).isNotNull();
        assertThat(first.getName()).isEqualTo("Coldplay Live");
        verify(eventRepository, times(1)).findByIdAndStatus(eq(eventId), eq(EventStatus.PUBLISHED));

        // 2nd call -> served from cache, repository NOT called again
        EventResponse second = eventService.getPublishedEventById(eventId);
        assertThat(second).isNotNull();
        assertThat(second.getName()).isEqualTo("Coldplay Live");
        verify(eventRepository, times(1)).findByIdAndStatus(eq(eventId), eq(EventStatus.PUBLISHED));
    }

    @Test
    @DisplayName("updateEvent evicts the event from published_events cache")
    void updateEvent_evictsCache() {
        UUID eventId = UUID.randomUUID();
        Event event = Event.builder()
                .id(eventId)
                .name("Coldplay Live")
                .venueName("DY Patil Stadium")
                .city("Mumbai")
                .startsAt(Instant.now().plusSeconds(86400))
                .endsAt(Instant.now().plusSeconds(97200))
                .currency("INR")
                .status(EventStatus.PUBLISHED)
                .organizerSubject("org-1")
                .build();

        when(eventRepository.findByIdAndStatus(eq(eventId), eq(EventStatus.PUBLISHED)))
                .thenReturn(Optional.of(event));
        when(eventRepository.findById(eq(eventId))).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Warm up cache
        eventService.getPublishedEventById(eventId);
        verify(eventRepository, times(1)).findByIdAndStatus(eq(eventId), eq(EventStatus.PUBLISHED));

        // Update event -> evicts cache
        UpdateEventRequest updateRequest = new UpdateEventRequest(
                "Coldplay Music of the Spheres",
                "World Tour",
                "DY Patil Stadium",
                "Mumbai",
                Instant.now().plusSeconds(86400),
                Instant.now().plusSeconds(97200),
                "INR",
                EventStatus.PUBLISHED
        );
        eventService.updateEvent(eventId, updateRequest, "org-1", false);

        // Next call should hit repository again
        eventService.getPublishedEventById(eventId);
        verify(eventRepository, times(2)).findByIdAndStatus(eq(eventId), eq(EventStatus.PUBLISHED));
    }
}
