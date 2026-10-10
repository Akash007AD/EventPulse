package com.eventpulse.notification.consumer;

import com.eventpulse.notification.config.RabbitMQConfig;
import com.eventpulse.notification.dto.NotificationPayload;
import com.eventpulse.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class RabbitNotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void consumeNotification(NotificationPayload payload) {
        log.info("Received notification job from RabbitMQ queue: recipient={}, type={}",
                payload.getRecipientSubject(), payload.getNotificationType());
        try {
            notificationService.sendNotification(payload);
        } catch (Exception ex) {
            log.error("Error processing notification job from queue: {}", ex.getMessage());
            throw ex; // triggers AMQP retry and eventually dead-letter exchange
        }
    }
}
