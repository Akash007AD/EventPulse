package com.eventpulse.event.dto;

import com.eventpulse.event.dto.validation.DateRanged;
import com.eventpulse.event.dto.validation.ValidDateRange;
import com.eventpulse.event.entity.EventStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidDateRange
public class UpdateEventRequest implements DateRanged {

    @NotBlank(message = "Event name must not be blank")
    @Size(max = 255, message = "Event name must not exceed 255 characters")
    private String name;

    @Size(max = 2000, message = "Event description must not exceed 2000 characters")
    private String description;

    @NotBlank(message = "Venue name must not be blank")
    @Size(max = 255, message = "Venue name must not exceed 255 characters")
    private String venueName;

    @NotBlank(message = "City must not be blank")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotNull(message = "Start time must be provided")
    private Instant startsAt;

    @NotNull(message = "End time must be provided")
    private Instant endsAt;

    @NotBlank(message = "Currency must not be blank")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code (e.g. INR, USD)")
    private String currency;

    private EventStatus status;

    private String posterUrl;

    private String seatingSchema;

    private java.util.List<PerformerDto> performers;

    public UpdateEventRequest(String name, String description, String venueName, String city,
            Instant startsAt, Instant endsAt, String currency, EventStatus status) {
        this.name = name;
        this.description = description;
        this.venueName = venueName;
        this.city = city;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.currency = currency;
        this.status = status;
    }
}
