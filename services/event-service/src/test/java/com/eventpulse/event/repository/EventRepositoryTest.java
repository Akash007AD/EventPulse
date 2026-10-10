package com.eventpulse.event.repository;

import com.eventpulse.event.entity.Event;
import com.eventpulse.event.entity.EventStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Test
    @DisplayName("findByStatus should only return events matching requested status")
    void findByStatus_returnsOnlyMatchingStatus() {
        Instant now = Instant.now();

        Event draftEvent = new Event(
                UUID.randomUUID(), "Draft Meetup", "Desc", "Hall A", "Kolkata",
                now.plus(1, ChronoUnit.DAYS), now.plus(2, ChronoUnit.DAYS), "INR", EventStatus.DRAFT, "org-1"
        );
        Event publishedEvent = new Event(
                UUID.randomUUID(), "Published Conference", "Desc", "Hall B", "Kolkata",
                now.plus(3, ChronoUnit.DAYS), now.plus(4, ChronoUnit.DAYS), "INR", EventStatus.PUBLISHED, "org-1"
        );
        Event archivedEvent = new Event(
                UUID.randomUUID(), "Archived Summit", "Desc", "Hall C", "Kolkata",
                now.plus(5, ChronoUnit.DAYS), now.plus(6, ChronoUnit.DAYS), "INR", EventStatus.ARCHIVED, "org-1"
        );

        eventRepository.save(draftEvent);
        eventRepository.save(publishedEvent);
        eventRepository.save(archivedEvent);

        Page<Event> publishedPage = eventRepository.findByStatus(EventStatus.PUBLISHED, PageRequest.of(0, 10));

        assertThat(publishedPage.getContent()).hasSize(1);
        assertThat(publishedPage.getContent().get(0).getName()).isEqualTo("Published Conference");
    }

    @Test
    @DisplayName("findByStatusAndCityIgnoreCase should filter by city case-insensitively")
    void findByStatusAndCityIgnoreCase_filtersCorrectly() {
        Instant now = Instant.now();

        Event kolkataEvent = new Event(
                UUID.randomUUID(), "Kolkata JUG", "Desc", "Venue 1", "Kolkata",
                now.plus(1, ChronoUnit.DAYS), now.plus(2, ChronoUnit.DAYS), "INR", EventStatus.PUBLISHED, "org-1"
        );
        Event mumbaiEvent = new Event(
                UUID.randomUUID(), "Mumbai Tech", "Desc", "Venue 2", "Mumbai",
                now.plus(3, ChronoUnit.DAYS), now.plus(4, ChronoUnit.DAYS), "INR", EventStatus.PUBLISHED, "org-1"
        );

        eventRepository.save(kolkataEvent);
        eventRepository.save(mumbaiEvent);

        Page<Event> results = eventRepository.findByStatusAndCityIgnoreCase(
                EventStatus.PUBLISHED, "kolkata", PageRequest.of(0, 10)
        );

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getName()).isEqualTo("Kolkata JUG");
    }

    @Test
    @DisplayName("findByIdAndStatus should return empty when event status differs")
    void findByIdAndStatus_returnsEmptyWhenStatusDiffers() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        Event draftEvent = new Event(
                id, "Draft Session", "Desc", "Venue", "Delhi",
                now.plus(1, ChronoUnit.DAYS), now.plus(2, ChronoUnit.DAYS), "INR", EventStatus.DRAFT, "org-1"
        );
        eventRepository.save(draftEvent);

        Optional<Event> publishedLookup = eventRepository.findByIdAndStatus(id, EventStatus.PUBLISHED);
        assertThat(publishedLookup).isEmpty();

        Optional<Event> draftLookup = eventRepository.findByIdAndStatus(id, EventStatus.DRAFT);
        assertThat(draftLookup).isPresent();
    }
}
