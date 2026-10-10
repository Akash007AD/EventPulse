package com.eventpulse.event.repository;

import com.eventpulse.event.entity.OrganizerApplication;
import com.eventpulse.event.entity.OrganizerApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizerApplicationRepository extends JpaRepository<OrganizerApplication, UUID> {

    Optional<OrganizerApplication> findFirstByUserSubjectOrderByCreatedAtDesc(String userSubject);

    Page<OrganizerApplication> findByStatus(OrganizerApplicationStatus status, Pageable pageable);
}
