package com.eventpulse.notification.service;

import com.eventpulse.notification.dto.NotificationResponse;
import com.eventpulse.notification.dto.SendTicketEmailRequest;

public interface TicketEmailService {
    NotificationResponse sendTicketDeliveryEmail(SendTicketEmailRequest request);
}
