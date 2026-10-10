package com.eventpulse.analytics.controller;

import com.eventpulse.analytics.dto.EventAnalyticsResponse;
import com.eventpulse.analytics.dto.PlatformOverviewResponse;
import com.eventpulse.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/events/{eventId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<EventAnalyticsResponse> getEventAnalytics(@PathVariable UUID eventId) {
        log.info("Fetching real-time analytics for event: {}", eventId);
        return ResponseEntity.ok(analyticsService.getEventAnalytics(eventId));
    }

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlatformOverviewResponse> getPlatformOverview() {
        log.info("Fetching platform-wide analytics overview");
        return ResponseEntity.ok(analyticsService.getPlatformOverview());
    }
}
