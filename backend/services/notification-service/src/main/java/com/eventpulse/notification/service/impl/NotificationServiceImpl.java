package com.eventpulse.notification.service.impl;

import com.eventpulse.notification.dto.NotificationPayload;
import com.eventpulse.notification.dto.NotificationResponse;
import com.eventpulse.notification.dto.PagedResponse;
import com.eventpulse.notification.entity.NotificationDelivery;
import com.eventpulse.notification.entity.NotificationStatus;
import com.eventpulse.notification.repository.NotificationDeliveryRepository;
import com.eventpulse.notification.service.NotificationDispatcher;
import com.eventpulse.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationDeliveryRepository deliveryRepository;
    private final NotificationDispatcher notificationDispatcher;

    @Override
    public NotificationResponse sendNotification(NotificationPayload payload) {
        log.info("Recording notification for recipient: {}, type: {}",
                payload.getRecipientSubject(), payload.getNotificationType());

        NotificationDelivery delivery = NotificationDelivery.builder()
                .id(UUID.randomUUID())
                .recipientSubject(payload.getRecipientSubject())
                .recipientEmail(payload.getRecipientEmail())
                .notificationType(payload.getNotificationType())
                .title(payload.getTitle())
                .body(payload.getBody())
                .status(NotificationStatus.PENDING)
                .attemptCount(0)
                .build();

        NotificationDelivery saved = deliveryRepository.save(delivery);

        // Attempt dispatch
        try {
            boolean success = notificationDispatcher.dispatch(saved);
            if (success) {
                saved.setStatus(NotificationStatus.SENT);
                saved.setAttemptCount(saved.getAttemptCount() + 1);
            } else {
                saved.setStatus(NotificationStatus.FAILED);
                saved.setAttemptCount(saved.getAttemptCount() + 1);
                saved.setLastError("Dispatcher returned false");
            }
        } catch (Exception ex) {
            log.error("Dispatch failure: {}", ex.getMessage());
            saved.setStatus(NotificationStatus.FAILED);
            saved.setAttemptCount(saved.getAttemptCount() + 1);
            saved.setLastError(ex.getMessage());
        }

        NotificationDelivery updated = deliveryRepository.save(saved);
        return NotificationResponse.fromEntity(updated);
    }

    @Override
    public NotificationResponse retryNotification(UUID id) {
        log.info("Retrying delivery for notification: {}", id);
        NotificationDelivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Notification not found: " + id));

        delivery.setAttemptCount(delivery.getAttemptCount() + 1);
        try {
            boolean success = notificationDispatcher.dispatch(delivery);
            if (success) {
                delivery.setStatus(NotificationStatus.SENT);
                delivery.setLastError(null);
            } else {
                delivery.setStatus(NotificationStatus.FAILED);
                delivery.setLastError("Retry dispatch returned false");
            }
        } catch (Exception ex) {
            delivery.setStatus(NotificationStatus.FAILED);
            delivery.setLastError(ex.getMessage());
        }

        NotificationDelivery updated = deliveryRepository.save(delivery);
        return NotificationResponse.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(UUID id) {
        NotificationDelivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Notification not found: " + id));
        return NotificationResponse.fromEntity(delivery);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getMyNotifications(String recipientSubject, int page, int size) {
        int boundedPage = Math.max(page, 0);
        int boundedSize = (size <= 0 || size > 100) ? 20 : size;

        Pageable pageable = PageRequest.of(boundedPage, boundedSize, Sort.by("createdAt").descending());
        Page<NotificationDelivery> deliveries = deliveryRepository.findByRecipientSubject(recipientSubject, pageable);

        return PagedResponse.of(deliveries, NotificationResponse::fromEntity);
    }
}
