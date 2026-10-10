package com.eventpulse.event.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class EventNotFoundException extends RuntimeException {

    private final UUID eventId;

    public EventNotFoundException(UUID eventId) {
        super("Event not found with ID: " + eventId);
        this.eventId = eventId;
    }
}
