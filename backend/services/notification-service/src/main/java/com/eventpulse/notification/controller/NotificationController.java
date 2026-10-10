package com.eventpulse.notification.controller;

import com.eventpulse.notification.dto.NotificationPayload;
import com.eventpulse.notification.dto.NotificationResponse;
import com.eventpulse.notification.dto.PagedResponse;
import com.eventpulse.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<PagedResponse<NotificationResponse>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt) {
        String recipientSubject = (jwt != null) ? jwt.getSubject() : "anonymous";
        return ResponseEntity.ok(notificationService.getMyNotifications(recipientSubject, page, size));
    }

    @PostMapping("/{id}/retry")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationResponse> retryNotification(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.retryNotification(id));
    }

    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')")
    public ResponseEntity<NotificationResponse> sendNotificationDirect(@RequestBody NotificationPayload payload) {
        NotificationResponse response = notificationService.sendNotification(payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
