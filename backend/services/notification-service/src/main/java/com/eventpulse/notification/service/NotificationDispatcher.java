package com.eventpulse.notification.service;

import com.eventpulse.notification.entity.NotificationDelivery;

public interface NotificationDispatcher {

    boolean dispatch(NotificationDelivery delivery);
}
