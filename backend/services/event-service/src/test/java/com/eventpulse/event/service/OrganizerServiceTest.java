package com.eventpulse.event.service;

import com.eventpulse.event.dto.ApplyOrganizerRequest;
import com.eventpulse.event.dto.OrganizerApplicationResponse;
import com.eventpulse.event.dto.PagedResponse;
import com.eventpulse.event.entity.OrganizerApplication;
import com.eventpulse.event.entity.OrganizerApplicationStatus;
import com.eventpulse.event.exception.DuplicateOrganizerApplicationException;
import com.eventpulse.event.exception.InvalidOrganizerApplicationStateException;
import com.eventpulse.event.exception.OrganizerApplicationNotFoundException;
import com.eventpulse.event.repository.OrganizerApplicationRepository;
import com.eventpulse.event.service.impl.OrganizerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizerServiceTest {

    @Mock
    private OrganizerApplicationRepository repository;

    @Mock
    private RolePromotionService rolePromotionService;

    @InjectMocks
    private OrganizerServiceImpl organizerService;

    private ApplyOrganizerRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = ApplyOrganizerRequest.builder()
                .organizationName("Global Beats Live")
                .contactEmail("contact@globalbeats.com")
                .contactPhone("+91-9876543210")
                .businessRegistrationNumber("GSTIN123456789")
                .websiteUrl("https://globalbeats.com")
                .description("Premier EDM and indie concert promoter.")
                .build();
    }

    @Test
    @DisplayName("apply: when user has no active applications, creates PENDING application")
    void apply_success_createsPending() {
        when(repository.findFirstByUserSubjectOrderByCreatedAtDesc("user-123"))
                .thenReturn(Optional.empty());

        when(repository.save(any(OrganizerApplication.class))).thenAnswer(invocation -> {
            OrganizerApplication app = invocation.getArgument(0);
            return app;
        });

        OrganizerApplicationResponse response = organizerService.apply(sampleRequest, "user-123");

        assertThat(response).isNotNull();
        assertThat(response.getOrganizationName()).isEqualTo("Global Beats Live");
        assertThat(response.getStatus()).isEqualTo(OrganizerApplicationStatus.PENDING);
        assertThat(response.getUserSubject()).isEqualTo("user-123");
    }

    @Test
    @DisplayName("apply: when user already has an APPROVED application, throws DuplicateOrganizerApplicationException")
    void apply_alreadyApproved_throwsConflict() {
        OrganizerApplication existing = OrganizerApplication.builder()
                .id(UUID.randomUUID())
                .userSubject("user-123")
                .status(OrganizerApplicationStatus.APPROVED)
                .build();

        when(repository.findFirstByUserSubjectOrderByCreatedAtDesc("user-123"))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> organizerService.apply(sampleRequest, "user-123"))
                .isInstanceOf(DuplicateOrganizerApplicationException.class)
                .hasMessageContaining("already an approved organizer");
    }

    @Test
    @DisplayName("apply: when user already has a PENDING application, throws DuplicateOrganizerApplicationException")
    void apply_alreadyPending_throwsConflict() {
        OrganizerApplication existing = OrganizerApplication.builder()
                .id(UUID.randomUUID())
                .userSubject("user-123")
                .status(OrganizerApplicationStatus.PENDING)
                .build();

        when(repository.findFirstByUserSubjectOrderByCreatedAtDesc("user-123"))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> organizerService.apply(sampleRequest, "user-123"))
                .isInstanceOf(DuplicateOrganizerApplicationException.class)
                .hasMessageContaining("pending review");
    }

    @Test
    @DisplayName("apply: when auto-approve is enabled, automatically marks APPROVED and triggers role promotion")
    void apply_autoApproveEnabled_approvesAndPromotes() {
        ReflectionTestUtils.setField(organizerService, "autoApprove", true);

        when(repository.findFirstByUserSubjectOrderByCreatedAtDesc("user-123"))
                .thenReturn(Optional.empty());

        when(repository.save(any(OrganizerApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrganizerApplicationResponse response = organizerService.apply(sampleRequest, "user-123");

        assertThat(response.getStatus()).isEqualTo(OrganizerApplicationStatus.APPROVED);
        assertThat(response.getReviewedBy()).isEqualTo("SYSTEM_AUTO_APPROVE");
        verify(rolePromotionService).promoteToOrganizer("user-123");
    }

    @Test
    @DisplayName("getMyApplication: returns latest application if found")
    void getMyApplication_found() {
        OrganizerApplication app = OrganizerApplication.builder()
                .id(UUID.randomUUID())
                .userSubject("user-123")
                .organizationName("Global Beats Live")
                .status(OrganizerApplicationStatus.PENDING)
                .build();

        when(repository.findFirstByUserSubjectOrderByCreatedAtDesc("user-123"))
                .thenReturn(Optional.of(app));

        OrganizerApplicationResponse response = organizerService.getMyApplication("user-123");
        assertThat(response).isNotNull();
        assertThat(response.getOrganizationName()).isEqualTo("Global Beats Live");
    }

    @Test
    @DisplayName("getMyApplication: throws OrganizerApplicationNotFoundException if none exists")
    void getMyApplication_notFound() {
        when(repository.findFirstByUserSubjectOrderByCreatedAtDesc("user-123"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> organizerService.getMyApplication("user-123"))
                .isInstanceOf(OrganizerApplicationNotFoundException.class);
    }

    @Test
    @DisplayName("approveApplication: when PENDING, updates status to APPROVED and calls role promotion")
    void approveApplication_success() {
        UUID appId = UUID.randomUUID();
        OrganizerApplication app = OrganizerApplication.builder()
                .id(appId)
                .userSubject("user-artist")
                .status(OrganizerApplicationStatus.PENDING)
                .build();

        when(repository.findById(appId)).thenReturn(Optional.of(app));
        when(repository.save(any(OrganizerApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrganizerApplicationResponse response = organizerService.approveApplication(appId, "admin-alex");

        assertThat(response.getStatus()).isEqualTo(OrganizerApplicationStatus.APPROVED);
        assertThat(response.getReviewedBy()).isEqualTo("admin-alex");
        verify(rolePromotionService).promoteToOrganizer("user-artist");
    }

    @Test
    @DisplayName("approveApplication: when not PENDING, throws InvalidOrganizerApplicationStateException")
    void approveApplication_notPending_throwsConflict() {
        UUID appId = UUID.randomUUID();
        OrganizerApplication app = OrganizerApplication.builder()
                .id(appId)
                .userSubject("user-artist")
                .status(OrganizerApplicationStatus.APPROVED)
                .build();

        when(repository.findById(appId)).thenReturn(Optional.of(app));

        assertThatThrownBy(() -> organizerService.approveApplication(appId, "admin-alex"))
                .isInstanceOf(InvalidOrganizerApplicationStateException.class)
                .hasMessageContaining("Only PENDING applications can be approved");
    }

    @Test
    @DisplayName("rejectApplication: when PENDING, updates status to REJECTED and records reason")
    void rejectApplication_success() {
        UUID appId = UUID.randomUUID();
        OrganizerApplication app = OrganizerApplication.builder()
                .id(appId)
                .userSubject("user-scammer")
                .status(OrganizerApplicationStatus.PENDING)
                .build();

        when(repository.findById(appId)).thenReturn(Optional.of(app));
        when(repository.save(any(OrganizerApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrganizerApplicationResponse response = organizerService.rejectApplication(appId, "admin-alex", "Invalid tax ID provided.");

        assertThat(response.getStatus()).isEqualTo(OrganizerApplicationStatus.REJECTED);
        assertThat(response.getReviewedBy()).isEqualTo("admin-alex");
        assertThat(response.getRejectionReason()).isEqualTo("Invalid tax ID provided.");
    }

    @Test
    @DisplayName("listApplications: returns paginated applications")
    void listApplications_success() {
        OrganizerApplication app = OrganizerApplication.builder()
                .id(UUID.randomUUID())
                .userSubject("user-1")
                .status(OrganizerApplicationStatus.PENDING)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findByStatus(eq(OrganizerApplicationStatus.PENDING), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(app), pageable, 1));

        PagedResponse<OrganizerApplicationResponse> result = organizerService.listApplications(OrganizerApplicationStatus.PENDING, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
    }
}
