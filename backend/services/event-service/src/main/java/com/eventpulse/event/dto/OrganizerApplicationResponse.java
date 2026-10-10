package com.eventpulse.event.dto;

import com.eventpulse.event.entity.OrganizerApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizerApplicationResponse {

    private UUID id;
    private String userSubject;
    private String organizationName;
    private String contactEmail;
    private String contactPhone;
    private String businessRegistrationNumber;
    private String websiteUrl;
    private String description;
    private OrganizerApplicationStatus status;
    private String rejectionReason;
    private String reviewedBy;
    private Instant reviewedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
