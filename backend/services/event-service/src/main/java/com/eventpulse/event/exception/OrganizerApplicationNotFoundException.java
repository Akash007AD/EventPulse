package com.eventpulse.event.exception;

import java.util.UUID;

public class OrganizerApplicationNotFoundException extends RuntimeException {
    public OrganizerApplicationNotFoundException(UUID id) {
        super("Organizer application not found with ID: " + id);
    }

    public OrganizerApplicationNotFoundException(String message) {
        super(message);
    }
}
