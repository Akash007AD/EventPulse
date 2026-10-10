package com.eventpulse.event.service;

import com.eventpulse.event.dto.ApplyOrganizerRequest;
import com.eventpulse.event.dto.OrganizerApplicationResponse;
import com.eventpulse.event.dto.PagedResponse;
import com.eventpulse.event.entity.OrganizerApplicationStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrganizerService {

    OrganizerApplicationResponse apply(ApplyOrganizerRequest request, String userSubject);

    OrganizerApplicationResponse getMyApplication(String userSubject);

    PagedResponse<OrganizerApplicationResponse> listApplications(OrganizerApplicationStatus status, Pageable pageable);

    OrganizerApplicationResponse approveApplication(UUID applicationId, String adminSubject);

    OrganizerApplicationResponse rejectApplication(UUID applicationId, String adminSubject, String reason);
}
