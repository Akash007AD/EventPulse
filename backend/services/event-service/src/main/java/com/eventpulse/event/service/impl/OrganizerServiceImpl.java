package com.eventpulse.event.service.impl;

import com.eventpulse.event.dto.ApplyOrganizerRequest;
import com.eventpulse.event.dto.OrganizerApplicationResponse;
import com.eventpulse.event.dto.PagedResponse;
import com.eventpulse.event.entity.OrganizerApplication;
import com.eventpulse.event.entity.OrganizerApplicationStatus;
import com.eventpulse.event.exception.DuplicateOrganizerApplicationException;
import com.eventpulse.event.exception.InvalidOrganizerApplicationStateException;
import com.eventpulse.event.exception.OrganizerApplicationNotFoundException;
import com.eventpulse.event.repository.OrganizerApplicationRepository;
import com.eventpulse.event.service.OrganizerService;
import com.eventpulse.event.service.RolePromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizerServiceImpl implements OrganizerService {

    private final OrganizerApplicationRepository repository;
    private final RolePromotionService rolePromotionService;

    @Value("${eventpulse.organizer.auto-approve:false}")
    private boolean autoApprove;

    @Override
    @Transactional
    public OrganizerApplicationResponse apply(ApplyOrganizerRequest request, String userSubject) {
        log.info("Processing organizer application for user: {}", userSubject);

        Optional<OrganizerApplication> existing = repository.findFirstByUserSubjectOrderByCreatedAtDesc(userSubject);
        if (existing.isPresent()) {
            OrganizerApplicationStatus existingStatus = existing.get().getStatus();
            if (existingStatus == OrganizerApplicationStatus.APPROVED) {
                throw new DuplicateOrganizerApplicationException("You are already an approved organizer.");
            }
            if (existingStatus == OrganizerApplicationStatus.PENDING) {
                throw new DuplicateOrganizerApplicationException("You already have an organizer application pending review.");
            }
        }

        OrganizerApplication application = OrganizerApplication.builder()
                .id(UUID.randomUUID())
                .userSubject(userSubject)
                .organizationName(request.getOrganizationName())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .businessRegistrationNumber(request.getBusinessRegistrationNumber())
                .websiteUrl(request.getWebsiteUrl())
                .description(request.getDescription())
                .build();

        if (autoApprove) {
            log.info("Auto-approve is enabled. Automatically approving organizer application for user: {}", userSubject);
            application.setStatus(OrganizerApplicationStatus.APPROVED);
            application.setReviewedBy("SYSTEM_AUTO_APPROVE");
            application.setReviewedAt(Instant.now());
            OrganizerApplication saved = repository.save(application);
            rolePromotionService.promoteToOrganizer(userSubject);
            return toResponse(saved);
        } else {
            application.setStatus(OrganizerApplicationStatus.PENDING);
            OrganizerApplication saved = repository.save(application);
            return toResponse(saved);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizerApplicationResponse getMyApplication(String userSubject) {
        return repository.findFirstByUserSubjectOrderByCreatedAtDesc(userSubject)
                .map(this::toResponse)
                .orElseThrow(() -> new OrganizerApplicationNotFoundException("No organizer application found for current user."));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrganizerApplicationResponse> listApplications(OrganizerApplicationStatus status, Pageable pageable) {
        Page<OrganizerApplication> page = (status != null)
                ? repository.findByStatus(status, pageable)
                : repository.findAll(pageable);

        return PagedResponse.of(page, this::toResponse);
    }

    @Override
    @Transactional
    public OrganizerApplicationResponse approveApplication(UUID applicationId, String adminSubject) {
        log.info("Admin '{}' approving organizer application '{}'", adminSubject, applicationId);

        OrganizerApplication application = repository.findById(applicationId)
                .orElseThrow(() -> new OrganizerApplicationNotFoundException(applicationId));

        if (application.getStatus() != OrganizerApplicationStatus.PENDING) {
            throw new InvalidOrganizerApplicationStateException(
                    "Cannot approve application with status: " + application.getStatus() + ". Only PENDING applications can be approved."
            );
        }

        application.setStatus(OrganizerApplicationStatus.APPROVED);
        application.setReviewedBy(adminSubject);
        application.setReviewedAt(Instant.now());

        OrganizerApplication saved = repository.save(application);
        rolePromotionService.promoteToOrganizer(saved.getUserSubject());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public OrganizerApplicationResponse rejectApplication(UUID applicationId, String adminSubject, String reason) {
        log.info("Admin '{}' rejecting organizer application '{}'", adminSubject, applicationId);

        OrganizerApplication application = repository.findById(applicationId)
                .orElseThrow(() -> new OrganizerApplicationNotFoundException(applicationId));

        if (application.getStatus() != OrganizerApplicationStatus.PENDING) {
            throw new InvalidOrganizerApplicationStateException(
                    "Cannot reject application with status: " + application.getStatus() + ". Only PENDING applications can be rejected."
            );
        }

        application.setStatus(OrganizerApplicationStatus.REJECTED);
        application.setReviewedBy(adminSubject);
        application.setRejectionReason(reason);
        application.setReviewedAt(Instant.now());

        OrganizerApplication saved = repository.save(application);
        return toResponse(saved);
    }

    private OrganizerApplicationResponse toResponse(OrganizerApplication app) {
        return OrganizerApplicationResponse.builder()
                .id(app.getId())
                .userSubject(app.getUserSubject())
                .organizationName(app.getOrganizationName())
                .contactEmail(app.getContactEmail())
                .contactPhone(app.getContactPhone())
                .businessRegistrationNumber(app.getBusinessRegistrationNumber())
                .websiteUrl(app.getWebsiteUrl())
                .description(app.getDescription())
                .status(app.getStatus())
                .rejectionReason(app.getRejectionReason())
                .reviewedBy(app.getReviewedBy())
                .reviewedAt(app.getReviewedAt())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }
}
