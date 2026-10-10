package com.eventpulse.notification.service.impl;

import com.eventpulse.notification.dto.NotificationPayload;
import com.eventpulse.notification.dto.NotificationResponse;
import com.eventpulse.notification.dto.SendTicketEmailRequest;
import com.eventpulse.notification.dto.TicketItemDto;
import com.eventpulse.notification.entity.NotificationType;
import com.eventpulse.notification.service.NotificationService;
import com.eventpulse.notification.service.TicketEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketEmailServiceImpl implements TicketEmailService {

    private final NotificationService notificationService;

    @Override
    public NotificationResponse sendTicketDeliveryEmail(SendTicketEmailRequest request) {
        log.info("Generating and dispatching ticket delivery email for booking: {}, recipient: {}",
                request.getBookingId(), request.getRecipientEmail());

        String emailHtml = buildTicketEmailHtml(request);
        String subject = "Your EventPulse Tickets - " + request.getEventTitle() + " [Booking #" + request.getBookingId().toString().substring(0, 8) + "]";

        Map<String, Object> params = new HashMap<>();
        params.put("bookingId", request.getBookingId());
        params.put("eventId", request.getEventId());
        params.put("ticketCount", request.getTickets() != null ? request.getTickets().size() : 0);

        NotificationPayload payload = NotificationPayload.builder()
                .recipientSubject(request.getRecipientSubject())
                .recipientEmail(request.getRecipientEmail())
                .notificationType(NotificationType.TICKET_DELIVERY)
                .title(subject)
                .body(emailHtml)
                .templateParams(params)
                .build();

        return notificationService.sendNotification(payload);
    }

    private String buildTicketEmailHtml(SendTicketEmailRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">");
        sb.append("<style>");
        sb.append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #0f172a; color: #f8fafc; padding: 24px; }");
        sb.append(".card { background-color: #1e293b; border-radius: 12px; padding: 24px; max-width: 600px; margin: 0 auto; border: 1px solid #334155; }");
        sb.append(".header { text-align: center; border-bottom: 1px solid #334155; padding-bottom: 16px; margin-bottom: 20px; }");
        sb.append(".brand { font-size: 24px; font-weight: 800; color: #6366f1; letter-spacing: -0.5px; }");
        sb.append(".event-title { font-size: 20px; font-weight: 700; color: #ffffff; margin-top: 8px; }");
        sb.append(".ticket-box { background-color: #0f172a; border: 1px dashed #6366f1; border-radius: 8px; padding: 16px; margin: 16px 0; text-align: center; }");
        sb.append(".qr-token { font-family: 'Courier New', monospace; font-size: 11px; word-break: break-all; background-color: #1e293b; padding: 10px; border-radius: 6px; color: #38bdf8; margin-top: 8px; }");
        sb.append(".badge { display: inline-block; background: #22c55e; color: #022c22; font-weight: 700; font-size: 12px; padding: 4px 10px; border-radius: 9999px; }");
        sb.append(".notice { font-size: 12px; color: #94a3b8; text-align: center; margin-top: 20px; }");
        sb.append("</style></head><body>");
        sb.append("<div class=\"card\">");
        sb.append("<div class=\"header\">");
        sb.append("<div class=\"brand\">EventPulse</div>");
        sb.append("<div class=\"event-title\">").append(escapeHtml(request.getEventTitle())).append("</div>");
        if (request.getVenue() != null) {
            sb.append("<div style=\"color: #94a3b8; font-size: 14px; margin-top: 4px;\">📍 ").append(escapeHtml(request.getVenue())).append("</div>");
        }
        sb.append("<div style=\"color: #94a3b8; font-size: 13px; margin-top: 4px;\">Booking Ref: ").append(request.getBookingId()).append("</div>");
        sb.append("</div>");

        sb.append("<p style=\"font-size: 14px; color: #cbd5e1;\">Present each QR token below at the gate scanner upon entry. Each QR code is cryptographically signed and admits 1 person (anti-passback protected).</p>");

        if (request.getTickets() != null) {
            for (TicketItemDto ticket : request.getTickets()) {
                sb.append("<div class=\"ticket-box\">");
                sb.append("<div style=\"display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;\">");
                sb.append("<span style=\"font-weight: 700; color: #ffffff;\">Ticket: ").append(escapeHtml(ticket.getTicketNumber())).append("</span>");
                sb.append("<span class=\"badge\">VALID FOR ENTRY</span>");
                sb.append("</div>");
                sb.append("<div style=\"font-size: 12px; color: #94a3b8;\">Gate Check-in Token:</div>");
                sb.append("<div class=\"qr-token\">").append(escapeHtml(ticket.getQrCodeToken())).append("</div>");
                sb.append("</div>");
            }
        }

        sb.append("<div class=\"notice\">");
        sb.append("⚠️ <strong>Single-Use Security Notice:</strong> QR codes are single-use. Screenshot duplicates or re-entry attempts will be rejected by gate scanners.<br/>");
        sb.append("Need assistance? Contact support@eventpulse.io");
        sb.append("</div>");

        sb.append("</div></body></html>");
        return sb.toString();
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
