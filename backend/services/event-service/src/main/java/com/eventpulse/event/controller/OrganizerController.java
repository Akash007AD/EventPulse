package com.eventpulse.event.controller;

import com.eventpulse.event.dto.ApplyOrganizerRequest;
import com.eventpulse.event.dto.OrganizerApplicationResponse;
import com.eventpulse.event.dto.PagedResponse;
import com.eventpulse.event.dto.RejectOrganizerRequest;
import com.eventpulse.event.entity.OrganizerApplicationStatus;
import com.eventpulse.event.service.OrganizerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/organizers")
@RequiredArgsConstructor
public class OrganizerController {

    private final OrganizerService organizerService;

    @PostMapping("/apply")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrganizerApplicationResponse> apply(
            @Valid @RequestBody ApplyOrganizerRequest request,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-User-Subject", required = false) String headerUserSubject
    ) {
        String userSubject = resolveSubject(jwt, headerUserSubject);
        log.info("Received organizer application from user: {}", userSubject);
        OrganizerApplicationResponse response = organizerService.apply(request, userSubject);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrganizerApplicationResponse> getMyApplication(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-User-Subject", required = false) String headerUserSubject
    ) {
        String userSubject = resolveSubject(jwt, headerUserSubject);
        OrganizerApplicationResponse response = organizerService.getMyApplication(userSubject);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/applications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<OrganizerApplicationResponse>> listApplications(
            @RequestParam(required = false) OrganizerApplicationStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PagedResponse<OrganizerApplicationResponse> response = organizerService.listApplications(status, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/applications/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizerApplicationResponse> approveApplication(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Admin-Subject", required = false) String headerAdminSubject
    ) {
        String adminSubject = resolveSubject(jwt, headerAdminSubject);
        OrganizerApplicationResponse response = organizerService.approveApplication(id, adminSubject);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/applications/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizerApplicationResponse> rejectApplication(
            @PathVariable UUID id,
            @Valid @RequestBody RejectOrganizerRequest request,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Admin-Subject", required = false) String headerAdminSubject
    ) {
        String adminSubject = resolveSubject(jwt, headerAdminSubject);
        OrganizerApplicationResponse response = organizerService.rejectApplication(id, adminSubject, request.getReason());
        return ResponseEntity.ok(response);
    }

    private String resolveSubject(Jwt jwt, String headerSubject) {
        if (jwt != null && jwt.getSubject() != null) {
            return jwt.getSubject();
        }
        if (headerSubject != null && !headerSubject.isBlank()) {
            return headerSubject;
        }
        return "anonymous-user";
    }
}
