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
    private Instant createdAt;
    private Instant updatedAt;

    public static EventResponse fromEntity(Event event) {
        if (event == null) {
            return null;
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
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
