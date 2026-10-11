package com.eventpulse.booking.controller;

import com.eventpulse.booking.dto.BookingResponse;
import com.eventpulse.booking.dto.CreateBookingRequest;
import com.eventpulse.booking.dto.PagedResponse;
import com.eventpulse.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final com.eventpulse.booking.service.GoogleCalendarService calendarService;
    private final com.eventpulse.booking.service.CostingService costingService;

    @PostMapping("/calculate-cost")
    public ResponseEntity<com.eventpulse.booking.dto.CostCalculationResponse> calculateCost(
            @RequestBody com.eventpulse.booking.dto.CostCalculationRequest request
    ) {
        com.eventpulse.booking.dto.CostCalculationResponse response = costingService.calculateCost(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @RequestHeader(name = "X-Customer-Subject", required = false) String headerCustomerSubject,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String customerSubject = resolveCustomerSubject(jwt, headerCustomerSubject);
        BookingResponse response = bookingService.createBooking(request, customerSubject);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(
            @PathVariable UUID id,
            @RequestHeader(name = "X-Customer-Subject", required = false) String headerCustomerSubject,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        String customerSubject = isAdmin ? null : resolveCustomerSubject(jwt, headerCustomerSubject);

        BookingResponse response = bookingService.getBookingById(id, customerSubject);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<PagedResponse<BookingResponse>> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(name = "X-Customer-Subject", defaultValue = "customer-default") String headerCustomerSubject,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String customerSubject = resolveCustomerSubject(jwt, headerCustomerSubject);
        PagedResponse<BookingResponse> response = bookingService.getMyBookings(customerSubject, page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable UUID id,
            @RequestHeader(name = "X-Customer-Subject", required = false) String headerCustomerSubject,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        String customerSubject = isAdmin ? null : resolveCustomerSubject(jwt, headerCustomerSubject);

        BookingResponse response = bookingService.cancelBooking(id, customerSubject);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/calendar/google")
    public ResponseEntity<com.eventpulse.booking.dto.GoogleCalendarEventResponse> syncToGoogleCalendar(
            @PathVariable UUID id,
            @RequestHeader(name = "X-Google-Access-Token", required = false) String headerGoogleToken,
            @RequestBody(required = false) com.eventpulse.booking.dto.GoogleCalendarSyncRequest bodyRequest,
            @RequestHeader(name = "X-Customer-Subject", required = false) String headerCustomerSubject,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        String token = (bodyRequest != null && bodyRequest.getGoogleAccessToken() != null && !bodyRequest.getGoogleAccessToken().isBlank())
                ? bodyRequest.getGoogleAccessToken()
                : headerGoogleToken;

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Google OAuth access token is required (pass via header X-Google-Access-Token or body)");
        }

        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        String customerSubject = isAdmin ? null : resolveCustomerSubject(jwt, headerCustomerSubject);

        com.eventpulse.booking.dto.GoogleCalendarEventResponse response = calendarService.syncBookingToGoogleCalendar(id, token, customerSubject);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/calendar.ics")
    public ResponseEntity<String> getBookingCalendarIcs(
            @PathVariable UUID id,
            @RequestHeader(name = "X-Customer-Subject", required = false) String headerCustomerSubject,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        String customerSubject = isAdmin ? null : resolveCustomerSubject(jwt, headerCustomerSubject);

        String ics = calendarService.generateIcsCalendar(id, customerSubject);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"booking-" + id + ".ics\"")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/calendar; charset=UTF-8"))
                .body(ics);
    }

    private String resolveCustomerSubject(Jwt jwt, String headerCustomerSubject) {
        if (jwt != null && jwt.getSubject() != null && !jwt.getSubject().isBlank()) {
            return jwt.getSubject();
        }
        return headerCustomerSubject;
    }
}
