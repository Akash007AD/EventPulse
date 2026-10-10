package com.eventpulse.event.controller;

import com.eventpulse.event.dto.CreateEventRequest;
import com.eventpulse.event.dto.EventResponse;
import com.eventpulse.event.dto.PagedResponse;
import com.eventpulse.event.entity.EventStatus;
import com.eventpulse.event.exception.EventNotFoundException;
import com.eventpulse.event.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
@AutoConfigureMockMvc(addFilters = false)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @MockBean
    private com.eventpulse.event.service.EventCalendarService calendarService;

    @Test
    @DisplayName("POST /api/v1/events should return 201 Created when request is valid")
    void createEvent_shouldReturn201() throws Exception {
        UUID id = UUID.randomUUID();
        Instant startsAt = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant endsAt = startsAt.plus(2, ChronoUnit.HOURS);

        CreateEventRequest request = new CreateEventRequest(
                "Spring AI Summit",
                "Deep dive into LLMs with Spring",
                "ITC Sonar",
                "Kolkata",
                startsAt,
                endsAt,
                "INR",
                EventStatus.DRAFT,
                "org-1"
        );

        EventResponse response = new EventResponse(
                id, "Spring AI Summit", "Deep dive into LLMs with Spring",
                "ITC Sonar", "Kolkata", startsAt, endsAt, "INR",
                EventStatus.DRAFT, "org-1", Instant.now(), Instant.now()
        );

        when(eventService.createEvent(any(CreateEventRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.name", is("Spring AI Summit")))
                .andExpect(jsonPath("$.city", is("Kolkata")))
                .andExpect(jsonPath("$.status", is("DRAFT")));
    }

    @Test
    @DisplayName("POST /api/v1/events should return 400 Bad Request with ProblemDetail when name is blank")
    void createEvent_shouldReturn400WhenNameBlank() throws Exception {
        Instant startsAt = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant endsAt = startsAt.plus(2, ChronoUnit.HOURS);

        CreateEventRequest request = new CreateEventRequest(
                "",
                "Description",
                "Venue",
                "Kolkata",
                startsAt,
                endsAt,
                "INR",
                EventStatus.DRAFT,
                "org-1"
        );

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Validation Failed")))
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.invalidParams.name", containsString("must not be blank")));
    }

    @Test
    @DisplayName("POST /api/v1/events should return 400 Bad Request when endsAt is before startsAt")
    void createEvent_shouldReturn400WhenEndsAtBeforeStartsAt() throws Exception {
        Instant startsAt = Instant.now().plus(2, ChronoUnit.DAYS);
        Instant endsAt = startsAt.minus(2, ChronoUnit.HOURS); // Invalid!

        CreateEventRequest request = new CreateEventRequest(
                "Invalid Dates Meetup",
                "Description",
                "Venue",
                "Kolkata",
                startsAt,
                endsAt,
                "INR",
                EventStatus.DRAFT,
                "org-1"
        );

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Validation Failed")))
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")));
    }

    @Test
    @DisplayName("POST /api/v1/events should return 400 Bad Request when currency is invalid")
    void createEvent_shouldReturn400WhenCurrencyInvalid() throws Exception {
        Instant startsAt = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant endsAt = startsAt.plus(2, ChronoUnit.HOURS);

        CreateEventRequest request = new CreateEventRequest(
                "Event",
                "Description",
                "Venue",
                "Kolkata",
                startsAt,
                endsAt,
                "TOOLONG", // Invalid ISO code
                EventStatus.DRAFT,
                "org-1"
        );

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.invalidParams.currency", containsString("3-letter ISO code")));
    }

    @Test
    @DisplayName("GET /api/v1/events should return 200 OK and paginated response")
    void getPublishedEvents_shouldReturn200() throws Exception {
        EventResponse item = new EventResponse(
                UUID.randomUUID(), "Tech Meetup", "Desc", "Venue", "Delhi",
                Instant.now().plus(1, ChronoUnit.DAYS), Instant.now().plus(2, ChronoUnit.DAYS),
                "INR", EventStatus.PUBLISHED, "org-1", Instant.now(), Instant.now()
        );
        PagedResponse<EventResponse> pagedResponse = new PagedResponse<>(
                List.of(item), 0, 20, 1, 1, true, true
        );

        when(eventService.getPublishedEvents(0, 20, null)).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/events?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name", is("Tech Meetup")))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.page", is(0)));
    }

    @Test
    @DisplayName("GET /api/v1/events/{id} should return 404 when not found")
    void getEventById_shouldReturn404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(eventService.getPublishedEventById(id)).thenThrow(new EventNotFoundException(id));

        mockMvc.perform(get("/api/v1/events/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Event Not Found")))
                .andExpect(jsonPath("$.code", is("EVENT_NOT_FOUND")))
                .andExpect(jsonPath("$.detail", containsString(id.toString())));
    }

    @Test
    @DisplayName("DELETE /api/v1/events/{id} should soft-deactivate and return 200 OK with ARCHIVED status")
    void archiveEvent_shouldReturn200WithArchivedStatus() throws Exception {
        UUID id = UUID.randomUUID();
        EventResponse archivedResponse = new EventResponse(
                id, "Old Event", "Desc", "Venue", "Delhi",
                Instant.now(), Instant.now(), "INR",
                EventStatus.ARCHIVED, "org-1", Instant.now(), Instant.now()
        );

        when(eventService.archiveEvent(eq(id))).thenReturn(archivedResponse);

        mockMvc.perform(delete("/api/v1/events/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.status", is("ARCHIVED")));
    }
}
