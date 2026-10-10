package com.eventpulse.event.controller;

import com.eventpulse.event.config.KeycloakJwtAuthenticationConverter;
import com.eventpulse.event.config.SecurityConfig;
import com.eventpulse.event.dto.CreateEventRequest;
import com.eventpulse.event.dto.EventResponse;
import com.eventpulse.event.dto.PagedResponse;
import com.eventpulse.event.dto.UpdateEventRequest;
import com.eventpulse.event.entity.EventStatus;
import com.eventpulse.event.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
@Import({SecurityConfig.class, KeycloakJwtAuthenticationConverter.class})
class EventSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("Public GET /api/v1/events allows unauthenticated access")
    void getEvents_unauthenticated_returns200() throws Exception {
        PagedResponse<EventResponse> emptyPage = PagedResponse.<EventResponse>builder()
                .content(Collections.emptyList())
                .page(0)
                .size(20)
                .totalElements(0)
                .totalPages(0)
                .isLast(true)
                .build();

        when(eventService.getPublishedEvents(0, 20, null)).thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/events without authentication returns 401 ProblemDetail")
    void createEvent_unauthenticated_returns401() throws Exception {
        Instant now = Instant.now();
        CreateEventRequest request = CreateEventRequest.builder()
                .name("Kolkata Tech Fest")
                .venueName("Science City")
                .city("Kolkata")
                .startsAt(now.plus(1, ChronoUnit.DAYS))
                .endsAt(now.plus(2, ChronoUnit.DAYS))
                .currency("INR")
                .build();

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("POST /api/v1/events with CUSTOMER role returns 403 Forbidden ProblemDetail")
    void createEvent_withCustomerRole_returns403() throws Exception {
        Instant now = Instant.now();
        CreateEventRequest request = CreateEventRequest.builder()
                .name("Kolkata Tech Fest")
                .venueName("Science City")
                .city("Kolkata")
                .startsAt(now.plus(1, ChronoUnit.DAYS))
                .endsAt(now.plus(2, ChronoUnit.DAYS))
                .currency("INR")
                .build();

        mockMvc.perform(post("/api/v1/events")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("POST /api/v1/events with ORGANIZER role returns 201 Created and derives organizerSubject")
    void createEvent_withOrganizerRole_returns201() throws Exception {
        Instant now = Instant.now();
        CreateEventRequest request = CreateEventRequest.builder()
                .name("Kolkata Tech Fest")
                .venueName("Science City")
                .city("Kolkata")
                .startsAt(now.plus(1, ChronoUnit.DAYS))
                .endsAt(now.plus(2, ChronoUnit.DAYS))
                .currency("INR")
                .build();

        UUID eventId = UUID.randomUUID();
        EventResponse response = EventResponse.builder()
                .id(eventId)
                .name(request.getName())
                .status(EventStatus.DRAFT)
                .organizerSubject("organizer-123")
                .build();

        when(eventService.createEvent(any(CreateEventRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/events")
                        .with(jwt().jwt(jwt -> jwt.subject("organizer-123"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ORGANIZER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(eventId.toString()))
                .andExpect(jsonPath("$.organizerSubject").value("organizer-123"));
    }

    @Test
    @DisplayName("PUT /api/v1/events/{id} by non-owner returns 403 Forbidden")
    void updateEvent_byNonOwner_returns403() throws Exception {
        UUID eventId = UUID.randomUUID();
        Instant now = Instant.now();
        UpdateEventRequest request = UpdateEventRequest.builder()
                .name("Updated Fest")
                .venueName("Science City")
                .city("Kolkata")
                .startsAt(now.plus(1, ChronoUnit.DAYS))
                .endsAt(now.plus(2, ChronoUnit.DAYS))
                .currency("INR")
                .build();

        when(eventService.updateEvent(eq(eventId), any(UpdateEventRequest.class), eq("organizer-hacker"), eq(false)))
                .thenThrow(new AccessDeniedException("You do not have permission to modify this event."));

        mockMvc.perform(put("/api/v1/events/{id}", eventId)
                        .with(jwt().jwt(jwt -> jwt.subject("organizer-hacker"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ORGANIZER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("PUT /api/v1/events/{id} with ADMIN role succeeds even if not original owner")
    void updateEvent_byAdmin_succeeds() throws Exception {
        UUID eventId = UUID.randomUUID();
        Instant now = Instant.now();
        UpdateEventRequest request = UpdateEventRequest.builder()
                .name("Admin Updated Fest")
                .venueName("Science City")
                .city("Kolkata")
                .startsAt(now.plus(1, ChronoUnit.DAYS))
                .endsAt(now.plus(2, ChronoUnit.DAYS))
                .currency("INR")
                .build();

        EventResponse response = EventResponse.builder()
                .id(eventId)
                .name("Admin Updated Fest")
                .status(EventStatus.PUBLISHED)
                .organizerSubject("organizer-original")
                .build();

        when(eventService.updateEvent(eq(eventId), any(UpdateEventRequest.class), eq("admin-user"), eq(true)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/events/{id}", eventId)
                        .with(jwt().jwt(jwt -> jwt.subject("admin-user"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Admin Updated Fest"));
    }
}
