package com.eventpulse.notification.service;

import com.eventpulse.notification.dto.NotificationPayload;
import com.eventpulse.notification.dto.NotificationResponse;
import com.eventpulse.notification.dto.PagedResponse;

import java.util.UUID;

public interface NotificationService {

    NotificationResponse sendNotification(NotificationPayload payload);

    NotificationResponse retryNotification(UUID id);

    NotificationResponse getNotificationById(UUID id);

    PagedResponse<NotificationResponse> getMyNotifications(String recipientSubject, int page, int size);
}
