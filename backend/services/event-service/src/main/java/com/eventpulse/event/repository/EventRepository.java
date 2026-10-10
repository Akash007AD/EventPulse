package com.eventpulse.event.repository;

import com.eventpulse.event.entity.Event;
import com.eventpulse.event.entity.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    Page<Event> findByStatusAndCityIgnoreCase(EventStatus status, String city, Pageable pageable);

    Optional<Event> findByIdAndStatus(UUID id, EventStatus status);
}
