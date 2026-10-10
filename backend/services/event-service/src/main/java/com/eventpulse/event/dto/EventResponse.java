package com.eventpulse.event.dto;

import com.eventpulse.event.entity.Event;
import com.eventpulse.event.entity.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private String name;
    private String description;
    private String venueName;
    private String city;
    private Instant startsAt;
    private Instant endsAt;
    private String currency;
    private EventStatus status;
    private String organizerSubject;
    private String posterUrl;
    private String seatingSchema;
    private java.util.List<PerformerDto> performers;
    private Instant createdAt;
    private Instant updatedAt;

    public EventResponse(UUID id, String name, String description, String venueName, String city,
                         Instant startsAt, Instant endsAt, String currency, EventStatus status,
                         String organizerSubject, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.venueName = venueName;
        this.city = city;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.currency = currency;
        this.status = status;
        this.organizerSubject = organizerSubject;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.performers = java.util.List.of();
    }

    public static EventResponse fromEntity(Event event) {
        if (event == null) {
            return null;
        }
        java.util.List<PerformerDto> performerList = null;
        if (event.getPerformers() != null) {
            try {
                performerList = event.getPerformers().stream()
                        .map(PerformerDto::fromEntity)
                        .toList();
            } catch (Exception ignored) {
                // Ignore if uninitialized lazy proxy in detached contexts
            }
        }
        return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .venueName(event.getVenueName())
                .city(event.getCity())
                .startsAt(event.getStartsAt())
                .endsAt(event.getEndsAt())
                .currency(event.getCurrency())
                .status(event.getStatus())
                .organizerSubject(event.getOrganizerSubject())
                .posterUrl(event.getPosterUrl())
                .seatingSchema(event.getSeatingSchema())
                .performers(performerList != null ? performerList : java.util.List.of())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
