package com.eventpulse.event.controller;

import com.eventpulse.event.dto.CreateEventRequest;
import com.eventpulse.event.dto.EventResponse;
import com.eventpulse.event.dto.PagedResponse;
import com.eventpulse.event.dto.UpdateEventRequest;
import com.eventpulse.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final com.eventpulse.event.service.EventCalendarService calendarService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (jwt != null) {
            request.setOrganizerSubject(jwt.getSubject());
        }
        EventResponse created = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<EventResponse>> getPublishedEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String city
    ) {
        PagedResponse<EventResponse> response = eventService.getPublishedEvents(page, size, city);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable UUID id) {
        EventResponse event = eventService.getPublishedEventById(id);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/{id}/calendar.ics")
    public ResponseEntity<String> getEventCalendarIcs(@PathVariable UUID id) {
        String icsContent = calendarService.generateIcsCalendar(id);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"event-" + id + ".ics\"")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/calendar; charset=UTF-8"))
                .body(icsContent);
    }

    @GetMapping("/{id}/calendar/google-url")
    public ResponseEntity<java.util.Map<String, String>> getGoogleCalendarUrl(@PathVariable UUID id) {
        String url = calendarService.generateGoogleCalendarUrl(id);
        return ResponseEntity.ok(java.util.Map.of("url", url));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventRequest request,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        String callerSubject = jwt != null ? jwt.getSubject() : null;
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        EventResponse updated = (callerSubject != null)
                ? eventService.updateEvent(id, request, callerSubject, isAdmin)
                : eventService.updateEvent(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<EventResponse> archiveEvent(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        String callerSubject = jwt != null ? jwt.getSubject() : null;
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        EventResponse archived = (callerSubject != null)
                ? eventService.archiveEvent(id, callerSubject, isAdmin)
                : eventService.archiveEvent(id);
        return ResponseEntity.ok(archived);
    }
}
