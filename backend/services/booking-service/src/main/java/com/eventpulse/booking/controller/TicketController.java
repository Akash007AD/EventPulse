package com.eventpulse.booking.controller;

import com.eventpulse.booking.dto.CheckInResponse;
import com.eventpulse.booking.dto.ScanQrRequest;
import com.eventpulse.booking.dto.TicketResponse;
import com.eventpulse.booking.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/scan")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<CheckInResponse> scanAndCheckIn(
            @Valid @RequestBody ScanQrRequest request,
            @RequestHeader(name = "X-Staff-Subject", required = false) String headerStaffSubject,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String checkedInBy = resolveUserIdentifier(jwt, headerStaffSubject, "gate-scanner");
        log.info("Ticket scan request received by staff: {} for event: {}", checkedInBy, request.getEventId());
        CheckInResponse response = ticketService.scanAndCheckIn(request, checkedInBy);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<TicketResponse> getTicketById(
            @PathVariable UUID id,
            @RequestHeader(name = "X-Customer-Subject", required = false) String headerCustomerSubject,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        boolean isStaffOrAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_ORGANIZER"));

        String currentSubject = isStaffOrAdmin ? null : resolveUserIdentifier(jwt, headerCustomerSubject, null);
        TicketResponse response = ticketService.getTicketById(id, currentSubject);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<List<TicketResponse>> getTicketsForBooking(
            @PathVariable UUID bookingId,
            @RequestHeader(name = "X-Customer-Subject", required = false) String headerCustomerSubject,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        boolean isStaffOrAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_ORGANIZER"));

        String currentSubject = isStaffOrAdmin ? null : resolveUserIdentifier(jwt, headerCustomerSubject, null);
        List<TicketResponse> responses = ticketService.getTicketsForBooking(bookingId, currentSubject);
        return ResponseEntity.ok(responses);
    }

    private String resolveUserIdentifier(Jwt jwt, String headerValue, String fallback) {
        if (jwt != null) {
            if (jwt.getClaimAsString("preferred_username") != null) {
                return jwt.getClaimAsString("preferred_username");
            }
            if (jwt.getSubject() != null && !jwt.getSubject().isBlank()) {
                return jwt.getSubject();
            }
        }
        if (headerValue != null && !headerValue.isBlank()) {
            return headerValue;
        }
        return fallback;
    }
}
