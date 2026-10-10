package com.eventpulse.notification.dto;

import com.eventpulse.notification.entity.NotificationDelivery;
import com.eventpulse.notification.entity.NotificationStatus;
import com.eventpulse.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID id;
    private String recipientSubject;
    private String recipientEmail;
    private NotificationType notificationType;
    private String title;
    private String body;
    private NotificationStatus status;
    private Integer attemptCount;
    private String lastError;
    private Instant createdAt;
    private Instant updatedAt;

    public static NotificationResponse fromEntity(NotificationDelivery entity) {
        return NotificationResponse.builder()
                .id(entity.getId())
                .recipientSubject(entity.getRecipientSubject())
                .recipientEmail(entity.getRecipientEmail())
                .notificationType(entity.getNotificationType())
                .title(entity.getTitle())
                .body(entity.getBody())
                .status(entity.getStatus())
                .attemptCount(entity.getAttemptCount())
                .lastError(entity.getLastError())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
