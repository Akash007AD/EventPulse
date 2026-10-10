package com.eventpulse.notification.dto;

import com.eventpulse.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPayload {

    private String recipientSubject;
    private String recipientEmail;
    private NotificationType notificationType;
    private String title;
    private String body;
    private Map<String, Object> templateParams;
}
