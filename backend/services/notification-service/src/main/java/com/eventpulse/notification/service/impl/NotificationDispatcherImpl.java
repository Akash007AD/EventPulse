package com.eventpulse.notification.service.impl;

import com.eventpulse.notification.entity.NotificationDelivery;
import com.eventpulse.notification.service.NotificationDispatcher;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationDispatcherImpl implements NotificationDispatcher {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${notification.mail.from:no-reply@eventpulse.com}")
    private String mailFrom;

    public NotificationDispatcherImpl(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public boolean dispatch(NotificationDelivery delivery) {
        String recipient = delivery.getRecipientEmail() != null ? delivery.getRecipientEmail() : "no-email";
        log.info("DISPATCHING [{}] to recipient '{}' ({}) - Title: '{}'",
                delivery.getNotificationType(),
                delivery.getRecipientSubject(),
                recipient,
                delivery.getTitle());
        log.debug("Message Body:\n{}", delivery.getBody());

        // Check if real SMTP sending is available and configured
        if (delivery.getRecipientEmail() != null && !delivery.getRecipientEmail().isBlank()) {
            if (mailSender != null && mailUsername != null && !mailUsername.isBlank()) {
                try {
                    MimeMessage message = mailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                    helper.setFrom(mailFrom, "EventPulse");
                    helper.setTo(delivery.getRecipientEmail());
                    helper.setSubject(delivery.getTitle() != null ? delivery.getTitle() : "EventPulse Ticket & Notification");
                    helper.setText(delivery.getBody() != null ? delivery.getBody() : "", true); // HTML content

                    mailSender.send(message);
                    log.info("Successfully delivered email via SMTP to {}", delivery.getRecipientEmail());
                    return true;
                } catch (Exception e) {
                    log.error("Failed to send email via SMTP to {}: {}", delivery.getRecipientEmail(), e.getMessage());
                    return false;
                }
            } else {
                log.info("SMTP credentials not configured (MAIL_USERNAME is empty). Logged ticket email for {} without SMTP dispatch.", recipient);
            }
        }

        return true;
    }
}

