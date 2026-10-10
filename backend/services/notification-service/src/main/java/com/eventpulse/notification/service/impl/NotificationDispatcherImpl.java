package com.eventpulse.notification.service.impl;

import com.eventpulse.notification.entity.NotificationDelivery;
import com.eventpulse.notification.service.NotificationDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationDispatcherImpl implements NotificationDispatcher {

    @Override
    public boolean dispatch(NotificationDelivery delivery) {
        log.info("DISPATCHING [{}] to recipient '{}' ({}) - Title: '{}'",
                delivery.getNotificationType(),
                delivery.getRecipientSubject(),
                delivery.getRecipientEmail() != null ? delivery.getRecipientEmail() : "no-email",
                delivery.getTitle());
        log.debug("Message Body:\n{}", delivery.getBody());
        return true;
    }
}
